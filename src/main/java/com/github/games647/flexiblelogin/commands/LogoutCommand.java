package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class LogoutCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public LogoutCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getPlayersOnlyLogoutMessage());
            return CommandResult.success();
        }

        Account account = plugin.getDatabase().getAccountIfPresent((Player) source);
        if (account == null || !account.isLoggedIn()) {
            source.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getNotLoggedInMessage());
        } else {
            source.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getSuccessfullyLoggedOutMessage());
            account.setLoggedIn(false);
        }

        return CommandResult.success();
    }
}
