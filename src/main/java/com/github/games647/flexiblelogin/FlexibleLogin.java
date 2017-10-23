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
import com.github.games647.flexiblelogin.listener.prevent.GriefPreventListener;
import com.github.games647.flexiblelogin.listener.prevent.PreventListener;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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

    private final Pattern validNamePattern = Pattern.compile("^\\w{2,16}$");

    private final ProtectionManager protectionManager = new ProtectionManager(this);
    private final Map<String, Integer> attempts = Maps.newConcurrentMap();

    private Settings configuration;
    private Database database;

    private Hasher hasher;

    @Inject
    public FlexibleLogin(Logger logger, Injector injector) {
        this.logger = logger;
        this.injector = injector;
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration =  injector.getInstance(Settings.class);
        init();
    }

    @Listener //Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = Sponge.getCommandManager();

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LoginCommand(this))
                .arguments(onlyOne(string(of("password"))))
                .build(), "login", "log");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new RegisterCommand(this))
                .arguments(GenericArguments
                        .optional(GenericArguments
                                .repeated(
                                        string(of("password")), 2)))
                .build(), "register", "reg");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new ChangePasswordCommand(this))
                .arguments(GenericArguments
                        .repeated(
                                string(of("password")), 2))
                .build(), "changepassword", "changepw");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new SetEmailCommand(this))
                .arguments(onlyOne(string(of("email"))))
                .build(), "setemail", "email");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new ForgotPasswordCommand(this))
                .build(), "forgotpassword", "forgot");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LogoutCommand(this))
                .build(), "logout");

        //admin commands
        commandDispatcher.register(this, CommandSpec.builder()
                .permission(PomData.NAME + ".admin")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand(this))
                        .build(), "reload", "rl")
                .child(CommandSpec.builder()
                        .executor(new UnregisterCommand(this))
                        .arguments(onlyOne(string(of("account"))))
                        .build(), "unregister", "unreg")
                .child(CommandSpec.builder()
                        .executor(new ForceRegisterCommand(this))
                        .arguments(
                                onlyOne(
                                        string(of("account"))), string(of("password")))
                        .build(), "register", "reg")
                .child(CommandSpec.builder()
                        .executor(new ResetPasswordCommand(this))
                        .arguments(
                                onlyOne(
                                        string(of("account"))), string(of("password")))
                        .build(), "resetpw", "resetpassword")
                .build(), PomData.NAME);

        //register events
        Sponge.getEventManager().registerListeners(this, new ConnectionListener(this));
        Sponge.getEventManager().registerListeners(this, new PreventListener(this));

        if (Sponge.getPluginManager().isLoaded("GriefPrevent")) {
            Sponge.getEventManager().registerListeners(this, new GriefPreventListener(this));
        }
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
