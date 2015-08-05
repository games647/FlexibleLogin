package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
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
            source.sendMessage(Texts.of(TextColors.DARK_RED, "Only players could logout"));
            return CommandResult.success();
        }

        Account account = plugin.getDatabase().getAccountIfPresent((Player) source);
        if (account == null || !account.isLoggedIn()) {
            source.sendMessage(Texts.of(TextColors.DARK_RED, "You aren't loggedIn"));
        } else {
            source.sendMessage(Texts.of(TextColors.DARK_GREEN, "Logged out"));
            account.setLoggedIn(false);
        }

        return CommandResult.success();
    }
}
