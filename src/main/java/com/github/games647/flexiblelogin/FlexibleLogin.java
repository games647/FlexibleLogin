/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
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
import com.github.games647.flexiblelogin.commands.ForgotPasswordCommand;
import com.github.games647.flexiblelogin.commands.LoginCommand;
import com.github.games647.flexiblelogin.commands.LogoutCommand;
import com.github.games647.flexiblelogin.commands.RegisterCommand;
import com.github.games647.flexiblelogin.commands.SetEmailCommand;
import com.github.games647.flexiblelogin.commands.admin.AccountsCommand;
import com.github.games647.flexiblelogin.commands.admin.ForceLoginCommand;
import com.github.games647.flexiblelogin.commands.admin.ForceRegisterCommand;
import com.github.games647.flexiblelogin.commands.admin.LastLoginCommand;
import com.github.games647.flexiblelogin.commands.admin.ReloadCommand;
import com.github.games647.flexiblelogin.commands.admin.ResetPasswordCommand;
import com.github.games647.flexiblelogin.commands.admin.UnregisterCommand;
import com.github.games647.flexiblelogin.config.General.HashingAlgorithm;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.hasher.BcryptHasher;
import com.github.games647.flexiblelogin.hasher.Hasher;
import com.github.games647.flexiblelogin.hasher.TOTP;
import com.github.games647.flexiblelogin.listener.ConnectionListener;
import com.github.games647.flexiblelogin.listener.prevent.GriefPreventListener;
import com.github.games647.flexiblelogin.listener.prevent.PreventListener;
import com.github.games647.flexiblelogin.storage.AuthMeDatabase;
import com.github.games647.flexiblelogin.storage.Database;
import com.github.games647.flexiblelogin.storage.FlexibleDatabase;
import com.github.games647.flexiblelogin.tasks.MessageTask;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Task;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION,
        url = PomData.URL, description = PomData.DESCRIPTION,
        authors = {"games647", "frogocomics", "Toranktto", "sgdc3", "nalimleinad"},
        dependencies = {
                @Dependency(id = "griefprevention", version = "[4.0.1,)", optional = true)
        })
public class FlexibleLogin {

    private final Logger logger;
    private final Injector injector;
    private final Settings config;
    private final CommandManager commandManager;

    @Inject
    private EventManager eventManager;

    @Inject
    private PluginManager pluginManager;

    private ProtectionManager protectionManager;
    private Database database;
    private Hasher hasher;

    @Inject
    FlexibleLogin(Logger logger, Injector injector, Settings settings) {
        this.logger = logger;
        this.config = settings;

        try {
            //if we are on old sponge version the command manager doesn't exist for injections
            injector.getBinding(CommandManager.class);
        } catch (ConfigurationException configEx) {
            injector = injector.createChildInjector(binder -> binder.bind(CommandManager.class)
                    .toInstance(Sponge.getCommandManager()));
        }

        this.injector = injector;
        this.commandManager = Sponge.getCommandManager();
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        protectionManager = injector.getInstance(ProtectionManager.class);

        init();
    }

    @Listener //Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        registerCommands();

        //register events
        eventManager.registerListeners(this, protectionManager);
        eventManager.registerListeners(this, injector.getInstance(ConnectionListener.class));
        eventManager.registerListeners(this, injector.getInstance(PreventListener.class));
        if (pluginManager.isLoaded("GriefPrevention")) {
            eventManager.registerListeners(this, injector.getInstance(GriefPreventListener.class));
        }
    }

    private void registerCommands() {
        List<String> loginAliases = new ArrayList<>(Arrays.asList("login", "log"));
        
        if(!this.config.getGeneral().isSupportSomeChatPlugins()) {
            loginAliases.add("l");
        }
        
        commandManager.register(this, injector.getInstance(LoginCommand.class).buildSpec(), loginAliases);
        commandManager.register(this, injector.getInstance(RegisterCommand.class).buildSpec(), "register", "reg");
        commandManager.register(this, injector.getInstance(LogoutCommand.class).buildSpec(), "logout");
        commandManager.register(this, injector.getInstance(SetEmailCommand.class).buildSpec(), "setemail", "email");
        commandManager.register(this, injector.getInstance(ChangePasswordCommand.class)
                .buildSpec(), "changepassword", "changepw", "cp");
        commandManager.register(this, injector.getInstance(ForgotPasswordCommand.class)
                .buildSpec(), "forgotpassword", "forgot");

        //admin commands
        commandManager.register(this, CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".admin")
                .child(injector.getInstance(ReloadCommand.class).buildSpec(), "reload", "rl")
                .child(injector.getInstance(UnregisterCommand.class).buildSpec(), "unregister", "unreg")
                .child(injector.getInstance(ForceRegisterCommand.class).buildSpec(), "register", "reg")
                .child(injector.getInstance(LastLoginCommand.class).buildSpec(), "lastlogin")
                .child(injector.getInstance(ResetPasswordCommand.class).buildSpec(), "resetpw", "resetpassword")
                .child(injector.getInstance(ForceLoginCommand.class).buildSpec(), "forcelogin")
                .child(injector.getInstance(AccountsCommand.class).buildSpec(), "accounts", "acc")
                .build(), PomData.ARTIFACT_ID, "fl");
    }

    @Listener
    public void onDisable(GameStoppingServerEvent stoppingEvent) {
        //run this task sync in order let it finish before the process ends
        if (database != null) {
            database.close();
        }
    }

    public void onReload() {
        //run this task sync in order let it finish before the process ends
        database.close();

        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);

        Server server = Sponge.getServer();
        server.getOnlinePlayers().forEach(protectionManager::unprotect);

        init();

        server.getOnlinePlayers().stream()
                .peek(protectionManager::protect)
                .parallel()
                .forEach(database::loadAccount);
    }

    private void init() {
        config.load();
        try {
            if (config.getGeneral().getSQL().getAuthMeTable().isEmpty()) {
                database = new FlexibleDatabase(logger, config);
                database.createTable(this, config);
            } else {
                database = new AuthMeDatabase(logger, config);
            }
        } catch (SQLException | UncheckedExecutionException | IOException ex) {
            logger.error("Cannot connect to auth storage", ex);
            Task.builder().execute(() -> Sponge.getServer().shutdown()).submit(this);
        }

        //use bcrypt as fallback for now
        hasher = new BcryptHasher();
        if (config.getGeneral().getHashAlgo() == HashingAlgorithm.TOTP) {
            hasher = new TOTP();
        }

        //schedule tasks
        Task.builder().execute(new MessageTask(this, config))
                .interval(config.getGeneral().getMessageInterval(), TimeUnit.SECONDS)
                .submit(this);
    }

    public Settings getConfigManager() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public Database getDatabase() {
        return database;
    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public Hasher getHasher() {
        //this is thread-safe because it won't change after config load
        return hasher;
    }
}
