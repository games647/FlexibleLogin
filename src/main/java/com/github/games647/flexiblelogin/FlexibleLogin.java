/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
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
import com.github.games647.flexiblelogin.commands.LastLoginCommand;
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
import com.github.games647.flexiblelogin.listener.prevent.GriefPreventListener;
import com.github.games647.flexiblelogin.listener.prevent.PreventListener;
import com.google.common.collect.Maps;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class FlexibleLogin {

    private final Logger logger;
    private final Injector injector;
    private final Settings configuration;

    private final Pattern validNamePattern = Pattern.compile("^\\w{2,16}$");
    private final Map<String, Integer> attempts = Maps.newConcurrentMap();

    private Database database;

    @Inject
    private ProtectionManager protectionManager;

    private Hasher hasher;

    @Inject
    FlexibleLogin(Logger logger, Injector injector, Settings settings) {
        this.logger = logger;
        this.configuration = settings;

        try {
            //if we are on old sponge version the command manager doesn't exist for injections
            injector.getBinding(CommandManager.class);
        } catch (ConfigurationException configEx) {
            injector = injector.createChildInjector(binder -> binder.bind(CommandManager.class)
                    .toInstance(Sponge.getCommandManager()));
        }

        this.injector = injector;
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        init();
    }

    @Listener //Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        registerCommands();

        //register events
        EventManager eventManager = Sponge.getEventManager();
        eventManager.registerListeners(this, injector.getInstance(ConnectionListener.class));
        eventManager.registerListeners(this, injector.getInstance(PreventListener.class));
        if (Sponge.getPluginManager().isLoaded("GriefPrevention")) {
            eventManager.registerListeners(this, injector.getInstance(GriefPreventListener.class));
        }
    }

    private void registerCommands() {
        CommandManager commandDispatcher = Sponge.getCommandManager();

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(LoginCommand.class))
                .arguments(onlyOne(string(of("password"))))
                .build(), "login", "log");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(RegisterCommand.class))
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .repeated(
                                        string(of("password")), 2)))
                .build(), "register", "reg");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(ChangePasswordCommand.class))
                .arguments(GenericArguments
                        .repeated(
                                string(of("password")), 2))
                .build(), "changepassword", "changepw");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(SetEmailCommand.class))
                .arguments(onlyOne(string(of("email"))))
                .build(), "setemail", "email");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(ForgotPasswordCommand.class))
                .build(), "forgotpassword", "forgot");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(injector.getInstance(LogoutCommand.class))
                .build(), "logout");

        //admin commands
        commandDispatcher.register(this, CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".admin")
                .child(CommandSpec.builder()
                        .executor(injector.getInstance(ReloadCommand.class))
                        .build(), "reload", "rl")
                .child(CommandSpec.builder()
                        .executor(injector.getInstance(UnregisterCommand.class))
                        .arguments(onlyOne(string(of("account"))))
                        .build(), "unregister", "unreg")
                .child(CommandSpec.builder()
                        .executor(injector.getInstance(ForceRegisterCommand.class))
                        .arguments(
                                onlyOne(
                                        string(of("account"))), string(of("password")))
                        .build(), "register", "reg")
                .child(CommandSpec.builder()
                        .executor(injector.getInstance(LastLoginCommand.class))
                        .arguments(onlyOne(string(of("account"))))
                        .build(), "lastlogin")
                .child(CommandSpec.builder()
                        .executor(injector.getInstance(ResetPasswordCommand.class))
                        .arguments(
                                onlyOne(
                                        string(of("account"))), string(of("password")))
                        .build(), "resetpw", "resetpassword")
                .build(), PomData.ARTIFACT_ID);
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

        Server server = Sponge.getServer();
        server.getOnlinePlayers().forEach(protectionManager::unprotect);

        init();

        server.getOnlinePlayers().stream()
                .peek(protectionManager::protect)
                .forEach(database::loadAccount);
    }

    private void init() {
        configuration.load();
        try {
            database = new Database(this);
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

    public boolean isValidName(String input) {
        return validNamePattern.matcher(input).matches();
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
