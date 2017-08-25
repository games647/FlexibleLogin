/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin;

import com.github.games647.flexiblelogin.commands.ChangePasswordCommand;
import com.github.games647.flexiblelogin.commands.ForceRegisterCommand;
import com.github.games647.flexiblelogin.commands.LoginCommand;
import com.github.games647.flexiblelogin.commands.LogoutCommand;
import com.github.games647.flexiblelogin.commands.RegisterCommand;
import com.github.games647.flexiblelogin.commands.ReloadCommand;
import com.github.games647.flexiblelogin.commands.ResetPasswordCommand;
import com.github.games647.flexiblelogin.commands.SetEmailCommand;
import com.github.games647.flexiblelogin.commands.UnregisterCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.hasher.BcryptHasher;
import com.github.games647.flexiblelogin.hasher.Hasher;
import com.github.games647.flexiblelogin.hasher.TOTP;
import com.github.games647.flexiblelogin.listener.ConnectionListener;
import com.github.games647.flexiblelogin.listener.PreventListener;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

@Plugin(id = com.github.games647.flexiblelogin.PomData.ARTIFACT_ID
        , name = com.github.games647.flexiblelogin.PomData.NAME
        , version = com.github.games647.flexiblelogin.PomData.VERSION
        , url = com.github.games647.flexiblelogin.PomData.URL
        , description = com.github.games647.flexiblelogin.PomData.DESCRIPTION)
public class FlexibleLogin {

    private static FlexibleLogin instance;

    public static FlexibleLogin getInstance() {
        return instance;
    }

    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    //We will place more than one config there (i.e. H2/SQLite database)
    private Path defaultConfigFile;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Map<String, Integer> attempts = Maps.newConcurrentMap();

    private Settings configuration;
    private Database database;
    private final ProtectionManager protectionManager;

    private Hasher hasher;

    @Inject
    public FlexibleLogin(Logger logger, PluginContainer pluginContainer, Game game) {
        instance = this;

        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.game = game;

        this.protectionManager = new ProtectionManager();
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        Optional<String> apiVersion = Sponge.getPlatform().getContainer(Component.API).getVersion();
        apiVersion.ifPresent(version -> {
            String targetVersion = com.github.games647.flexiblelogin.PomData.SPONGE_VERSION;
            if (!version.split("\\.")[0].equals(targetVersion.split("\\.")[0])) {
                logger.warn("Major sponge version doesn't equal the target version of this plugin");
                Sponge.getServer().shutdown();
            }
        });

        configuration = new Settings(configManager, defaultConfigFile);
        configuration.load();

        database = new Database();
        database.createTable();

        if ("totp".equalsIgnoreCase(configuration.getConfig().getHashAlgo())) {
            hasher = new TOTP();
        } else {
            //use BCrypt as fallback for now
            hasher = new BcryptHasher();
        }
    }

    @Listener //Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = game.getCommandManager();

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LoginCommand())
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .build(), "login", "log");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new RegisterCommand())
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .repeated(GenericArguments
                                        .string(Text.of("password")), 2)))
                .build(), "register", "reg");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new ChangePasswordCommand())
                .arguments(GenericArguments
                        .repeated(GenericArguments
                                .string(Text.of("password")), 2))
                .build(), "changepassword", "changepw");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new SetEmailCommand())
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))))
                .build(), "setemail", "email");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new UnregisterCommand())
                .build(), "forgotpassword", "forgot");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LogoutCommand())
                .build(), "logout");

        //admin commands
        commandDispatcher.register(this, CommandSpec.builder()
                .permission(pluginContainer.getName() + ".admin")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand())
                        .build(), "reload", "rl")
                .child(CommandSpec.builder()
                        .executor(new UnregisterCommand())
                        .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("account"))))
                        .build(), "unregister", "unreg")
                .child(CommandSpec.builder()
                        .executor(new ForceRegisterCommand())
                        .arguments(
                                GenericArguments.onlyOne(GenericArguments
                                        .string(Text.of("account"))), GenericArguments.string(Text.of("password")))
                        .build(), "register", "reg")
                .child(CommandSpec.builder()
                        .executor(new ResetPasswordCommand())
                        .arguments(
                                GenericArguments.onlyOne(GenericArguments
                                        .string(Text.of("account"))), GenericArguments.string(Text.of("password")))
                        .build(), "resetpw", "resetpassword")
                .build(), pluginContainer.getName());

        //register events
        game.getEventManager().registerListeners(this, new ConnectionListener());
        game.getEventManager().registerListeners(this, new PreventListener());
    }

    @Listener
    public void onDisable(GameStoppedServerEvent gameStoppedEvent) {
        //run this task sync in order let it finish before the process ends
        database.close();

        game.getServer().getOnlinePlayers().forEach(protectionManager::unprotect);
    }

    public void onReload() {
        //run this task sync in order let it finish before the process ends
        database.close();

        game.getServer().getOnlinePlayers().forEach(protectionManager::unprotect);

        configuration.load();
        database = new Database();
        database.createTable();

        if ("totp".equalsIgnoreCase(configuration.getConfig().getHashAlgo())) {
            hasher = new TOTP();
        } else {
            //use bcrypt as fallback for now
            hasher = new BcryptHasher();
        }

        game.getServer().getOnlinePlayers().forEach(protectionManager::protect);
        game.getServer().getOnlinePlayers().forEach(database::loadAccount);
    }

    public Settings getConfigManager() {
        return configuration;
    }

    public PluginContainer getContainer() {
        return pluginContainer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public Database getDatabase() {
        return database;
    }

    public Map<String, Integer> getAttempts() {
        return attempts;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public Hasher getHasher() {
        //this is thread-safe because it won't change after config load
        return hasher;
    }
}
