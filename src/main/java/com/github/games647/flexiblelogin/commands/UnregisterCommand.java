package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.UnregisterTask;

import java.util.UUID;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class UnregisterCommand implements CommandExecutor {

    private static final String UUID_REGEX
            = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";
    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin;

    public UnregisterCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String account = args.<String>getOne("account").get();
        if (account.matches(UUID_REGEX)) {
            //check if the account is an UUID
            UUID uuid = UUID.fromString(account);
            plugin.getGame().getScheduler().getTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new UnregisterTask(plugin, src, uuid))
                    .submit(plugin);
            return CommandResult.success();
        } else if (account.matches(VALID_USERNAME)) {
            //check if the account is a valid player name
            plugin.getGame().getScheduler().getTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new UnregisterTask(plugin, src, account))
                    .submit(plugin);
            return CommandResult.success();
        }

        src.sendMessage(Texts.of(TextColors.DARK_RED, "Your request is neither a valid player name or UUID"));

        return CommandResult.success();
    }
}
