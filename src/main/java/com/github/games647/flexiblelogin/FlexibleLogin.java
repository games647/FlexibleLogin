package com.github.games647.flexiblelogin;

import com.google.inject.Inject;

import com.github.games647.flexiblelogin.commands.LoginCommand;
import com.github.games647.flexiblelogin.commands.LogoutCommand;
import com.github.games647.flexiblelogin.commands.RegisterCommand;
import com.github.games647.flexiblelogin.commands.SetEmailCommand;
import com.github.games647.flexiblelogin.commands.UnregisterCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.hasher.BcryptHasher;
import com.github.games647.flexiblelogin.hasher.Hasher;
import com.github.games647.flexiblelogin.hasher.TOTP;
import com.github.games647.flexiblelogin.listener.PlayerListener;
import com.github.games647.flexiblelogin.listener.PreventListener;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "flexiblelogin", name = "FlexibleLogin", version = "0.2.7")
public class FlexibleLogin {

    private static FlexibleLogin instance = new FlexibleLogin();

    public static FlexibleLogin getInstance() {
        return instance;
    }

    @Inject private PluginContainer pluginContainer;
    @Inject private Logger logger;
    @Inject private Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    //We will place more than one config there (i.e. H2/SQLite database)
    private File defaultConfigFile;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Settings configuration;
    private Database database;

    private Hasher hasher;

    private FlexibleLogin() {}

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        logger.info("Loading {} v{}", pluginContainer.getName(), pluginContainer.getVersion());

        configuration = new Settings(configManager, defaultConfigFile, this);
        configuration.load();

        database = new Database(this);
        database.createTable();

        if (configuration.getConfiguration().getHashAlgo().equalsIgnoreCase("totp")) {
            hasher = new TOTP();
        } else {
            //use bcrypt as fallback for now
            hasher = new BcryptHasher();
        }
    }

    @Listener //During this state, the plugin should finish any work needed in order to be functional. Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandService commandDispatcher = initEvent.getGame().getCommandDispatcher();

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LoginCommand(this))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Texts.of("password"))))
                .build(), "login");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new RegisterCommand(this))
                .arguments(
                        GenericArguments
                                .optional(GenericArguments
                                        .repeated(GenericArguments
                                                .string(Texts.of("password")), 2)))
                .build(), "register");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new LogoutCommand(this))
                .build(), "logout");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new UnregisterCommand(this))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Texts.of("account"))))
                .permission(pluginContainer.getName() + ".admin")
                .build(), "unregister");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new SetEmailCommand(this))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Texts.of("email"))))
                .build(), "setemail");

        commandDispatcher.register(this, CommandSpec.builder()
                .executor(new UnregisterCommand(this))
                .build(), "forgotpassword");

        //register events
        initEvent.getGame().getEventManager().registerListeners(this, new PlayerListener(this));
        initEvent.getGame().getEventManager().registerListeners(this, new PreventListener(this));
    }

//    @Subscribe
//    public void onPostInit(PostInitializationEvent postInitEvent) {
//        //inter-plugin communication + Plugins providing an API should be ready to accept basic requests.
//    }

//    @Listener
//    public void onServerStart(GameAboutToStartServerEvent serverAboutToStartEvent) {
//        //The server instance exists, but worlds are not yet loaded.
//    }

//    @Subscribe
//    public void onServerStopping(ServerStoppingEvent serverStoppingEvent) {
//        //This state occurs immediately before the final tick, before the worlds are saved.
//    }

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

    public Hasher getHasher() {
        //this is thread-safe because it won't change after config load
        return hasher;
    }
}
