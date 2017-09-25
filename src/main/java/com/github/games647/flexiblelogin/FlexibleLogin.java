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
import com.github.games647.flexiblelogin.commands.ForgotPasswordCommand;
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
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class FlexibleLogin {

    private static FlexibleLogin instance;

    public static FlexibleLogin getInstance() {
        return instance;
    }

    private final Logger logger;

    private final Pattern validNamePattern = Pattern.compile("^\\w{2,16}$");
    private final Pattern uuidPattern = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    @Inject
    @ConfigDir(sharedRoot = false)
    //We will place more than one config there (i.e. H2/SQLite database)
    private Path dataFolder;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private final ProtectionManager protectionManager = new ProtectionManager();
    private final Map<String, Integer> attempts = Maps.newConcurrentMap();

    private Settings configuration;
    private Database database;

    private Hasher hasher;

    @Inject
    public FlexibleLogin(Logger logger) {
        instance = this;

        this.logger = logger;
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration = new Settings(dataFolder);
        init();
    }

    @Listener //Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = Sponge.getCommandManager();

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LoginCommand())
                .arguments(onlyOne(string(Text.of("password"))))
                .build(), "login", "log");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new RegisterCommand())
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .repeated(
                                        string(Text.of("password")), 2)))
                .build(), "register", "reg");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new ChangePasswordCommand())
                .arguments(GenericArguments
                        .repeated(
                                string(Text.of("password")), 2))
                .build(), "changepassword", "changepw");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new SetEmailCommand())
                .arguments(onlyOne(string(Text.of("email"))))
                .build(), "setemail", "email");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new ForgotPasswordCommand())
                .build(), "forgotpassword", "forgot");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LogoutCommand())
                .build(), "logout");

        //admin commands
        commandDispatcher.register(this, CommandSpec.builder()
                .permission(PomData.NAME + ".admin")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand())
                        .build(), "reload", "rl")
                .child(CommandSpec.builder()
                        .executor(new UnregisterCommand())
                        .arguments(onlyOne(string(Text.of("account"))))
                        .build(), "unregister", "unreg")
                .child(CommandSpec.builder()
                        .executor(new ForceRegisterCommand())
                        .arguments(
                                onlyOne(
                                        string(Text.of("account"))), string(Text.of("password")))
                        .build(), "register", "reg")
                .child(CommandSpec.builder()
                        .executor(new ResetPasswordCommand())
                        .arguments(
                                onlyOne(
                                        string(Text.of("account"))), string(Text.of("password")))
                        .build(), "resetpw", "resetpassword")
                .build(), PomData.NAME);

        //register events
        Sponge.getEventManager().registerListeners(this, new ConnectionListener());
        Sponge.getEventManager().registerListeners(this, new PreventListener());
    }

    @Listener
    public void onDisable(GameStoppingServerEvent stoppingEvent) {
        //run this task sync in order let it finish before the process ends
        database.close();

        Sponge.getServer().getOnlinePlayers().forEach(protectionManager::unprotect);
    }

    public void onReload() {
        //run this task sync in order let it finish before the process ends
        database.close();

        Sponge.getServer().getOnlinePlayers().forEach(protectionManager::unprotect);

        init();

        Sponge.getServer().getOnlinePlayers().forEach(protectionManager::protect);
        Sponge.getServer().getOnlinePlayers().forEach(database::loadAccount);
    }

    private void init() {
        configuration.load();
        try {
            database = new Database();
            database.createTable();
        } catch (SQLException sqlEx) {
            logger.error("Cannot connect to auth storage", sqlEx);
            Sponge.getServer().shutdown();
        }

        if ("totp".equalsIgnoreCase(configuration.getGeneral().getHashAlgo())) {
            hasher = new TOTP();
        } else {
            //use bcrypt as fallback for now
            hasher = new BcryptHasher();
        }
    }

    public void checkPlayerPermission(Subject player, String key) throws CommandPermissionException {
        if (configuration.getGeneral().isPlayerPermissions()
                && !player.hasPermission(PomData.ARTIFACT_ID + ".command." + key)) {
            throw new CommandPermissionException();
        }
    }

    public boolean isValidName(String input) {
        return validNamePattern.matcher(input).matches();
    }

    public boolean isValidUUID(String input) {
        return uuidPattern.matcher(input).matches();
    }

    public Settings getConfigManager() {
        return configuration;
    }

    public Logger getLogger() {
        return logger;
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
