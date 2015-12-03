package com.github.games647.flexiblelogin.commands;

import com.google.common.collect.Lists;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.RegisterTask;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Collection;
import java.util.List;

public class RegisterCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public RegisterCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(plugin.getConfigManager().getConfiguration().getTextConfiguration().getPlayersOnlyRegisterMessage());
            return CommandResult.success();
        }

        //If the server is using TOTP, no password is required
        if (!args.hasAny("password")) {
            if (plugin.getConfigManager().getConfiguration().getHashAlgo().equals("totp")) {
                startTask(source, "");
            } else {
                source.sendMessage(plugin.getConfigManager().getConfiguration().getTextConfiguration().getTotpNotEnabledMessage());
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
            source.sendMessage(plugin.getConfigManager().getConfiguration().getTextConfiguration().getUnequalPasswordsMessage());
        }

        return CommandResult.success();
    }

    private void startTask(CommandSource source, String password) {
        plugin.getGame().getScheduler().createTaskBuilder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new RegisterTask(plugin, (Player) source, password))
                .name("Register Query")
                .submit(plugin);
    }
}
