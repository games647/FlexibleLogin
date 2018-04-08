package com.github.games647.flexiblelogin.commands.register;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.RegisterTask;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class TwoFactorRegisterCommand extends AbstractCommand implements RegisterCommand {

    @Inject
    private ProtectionManager protectionManager;

    @Inject
    TwoFactorRegisterCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(settings.getText().getPlayersOnly());
        }

        checkPlayerPermission(src);

        Task.builder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new RegisterTask(plugin, protectionManager, (Player) src))
                .name("Register Query")
                .submit(plugin);

        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .build();
    }
}
