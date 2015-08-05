package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.LoginTask;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class LoginCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public LoginCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Texts.of(TextColors.DARK_RED, "Only players need to login"));
        }

        //the arg isn't optional. We can be sure there is value
        String password = args.<String>getOne("password").get();
        plugin.getGame().getScheduler().getTaskBuilder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new LoginTask(plugin, (Player) source, password))
                .name("Login Query")
                .submit(plugin);

        return CommandResult.success();
    }
}
