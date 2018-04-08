package com.github.games647.flexiblelogin.commands.register;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.RegisterTask;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.repeated;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class PasswordRegisterCommand extends AbstractCommand implements RegisterCommand {

    @Inject
    private ProtectionManager protectionManager;

    @Inject
    PasswordRegisterCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(settings.getText().getPlayersOnly());
        }

        checkPlayerPermission(src);
        Collection<String> passwords = args.getAll("password");
        List<String> indexPasswords = Lists.newArrayList(passwords);
        String password = indexPasswords.get(0);
        if (!password.equals(indexPasswords.get(1))) {
            //Check if the first two passwords are equal to prevent typos
            throw new CommandException(settings.getText().getUnequalPasswords());
        }

        if (password.length() < settings.getGeneral().getMinPasswordLength()) {
            throw new CommandException(settings.getText().getTooShortPassword());
        }

        Task.builder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new RegisterTask(plugin, protectionManager, (Player) src, password))
                .name("Register Query")
                .submit(plugin);
        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(
                        repeated(
                                string(of("password")),
                                2)
                )
                .build();
    }
}
