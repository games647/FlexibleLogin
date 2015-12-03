package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.SaveTask;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class SetEmailCommand implements CommandExecutor {

    private static final String EMAIL_REGEX = "^[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$";

    private final FlexibleLogin plugin;

    public SetEmailCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getPlayersOnlySetEmail());
            return CommandResult.empty();
        }

        String email = args.<String>getOne("email").get();
        if (email.matches(EMAIL_REGEX)) {
            Account account = plugin.getDatabase().getAccountIfPresent((Player) src);
            if (account != null) {
                account.setEmail(email);
                src.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getEmailSetMessage());
                plugin.getGame().getScheduler().createTaskBuilder()
                        .async()
                        .execute(new SaveTask(plugin, account))
                        .submit(plugin);
            }

            return CommandResult.success();
        }

        src.sendMessage(FlexibleLogin.getInstance().getConfigManager().getConfiguration().getTextConfiguration().getNotEmailMessage());
        return CommandResult.success();
    }
}
