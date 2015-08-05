package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.RegisterTask;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class RegisterCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public RegisterCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Texts.of(TextColors.DARK_RED, "Only players need to register"));
            return CommandResult.success();
        }

        //If the server is using TOTP, no password is required
        if (!args.hasAny("password")) {
            if (plugin.getConfigManager().getConfiguration().getHashAlgo().equals("totp")) {
                startTask(source, "");
            } else {
                source.sendMessage(Texts.of(TextColors.DARK_RED
                        , "TOTP isn't enabled. You have to enter two passwords"));
            }

            return CommandResult.success();
        }

        Collection<String> passwords = args.<String>getAll("password");
        List<String> indexPasswords = Lists.newArrayList(passwords);
        String password = indexPasswords.get(0);
        if (password.equals(indexPasswords.get(1))) {
            //Check if the first two passwords are equal to prevent typos
            startTask(source, password);
        } else {
            source.sendMessage(Texts.of(TextColors.DARK_RED, "The passwords are not equal"));
        }

        return CommandResult.success();
    }

    private void startTask(CommandSource source, String password) {
        plugin.getGame().getScheduler().getTaskBuilder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new RegisterTask(plugin, (Player) source, password))
                .name("Register Query")
                .submit(plugin);
    }
}
