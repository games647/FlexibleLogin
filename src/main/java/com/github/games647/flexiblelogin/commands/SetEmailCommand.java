package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.SaveTask;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

public class SetEmailCommand implements CommandExecutor {

    private static final String EMAIL_REGEX = "^[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$";

    private final FlexibleLogin plugin;

    public SetEmailCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "Only players can set their own email"));
            return CommandResult.empty();
        }

        String email = args.<String>getOne("email").get();
        if (email.matches(EMAIL_REGEX)) {
            Account account = plugin.getDatabase().getAccountIfPresent((Player) src);
            if (account != null) {
                account.setEmail(email);
                src.sendMessage(Texts.of(TextColors.DARK_GREEN, "Your email was set"));
                plugin.getGame().getScheduler().createTaskBuilder()
                        .async()
                        .execute(new SaveTask(plugin, account))
                        .submit(plugin);
            }

            return CommandResult.success();
        }

        src.sendMessage(Texts.of(TextColors.DARK_RED, "Your input isn't a valid email adress"));
        return CommandResult.success();
    }
}
