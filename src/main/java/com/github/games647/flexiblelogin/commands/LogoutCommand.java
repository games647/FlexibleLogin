package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class LogoutCommand implements CommandExecutor {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getPlayersOnlyActionMessage());
            return CommandResult.success();
        }

        Account account = plugin.getDatabase().getAccountIfPresent((Player) source);
        if (account == null || !account.isLoggedIn()) {
            source.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getNotLoggedInMessage());
        } else {
            source.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getSuccessfullyLoggedOutMessage());
            account.setLoggedIn(false);
        }

        return CommandResult.success();
    }
}
