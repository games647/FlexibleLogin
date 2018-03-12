/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.AttemptManager;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.LoginTask;
import com.google.inject.Inject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class LoginCommand extends AbstractCommand {

    private final AttemptManager attemptManager;
    private final CommandManager commandManager;

    @Inject
    LoginCommand(FlexibleLogin plugin, Logger logger, Settings settings, AttemptManager attemptManager,
                 CommandManager commandManager) {
        super(plugin, logger, settings, "login");

        this.attemptManager = attemptManager;
        this.commandManager = commandManager;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(settings.getText().getPlayersOnly());
            return CommandResult.empty();
        }

        checkPlayerPermission(src);

        Player player = (Player) src;

        if (plugin.getDatabase().isLoggedIn(player)) {
            src.sendMessage(settings.getText().getAlreadyLoggedIn());
        }

        UUID uniqueId = player.getUniqueId();
        if (!attemptManager.isAllowed(uniqueId)) {
            src.sendMessage(settings.getText().getMaxAttempts());
            String lockCommand = settings.getGeneral().getLockCommand();
            if (!lockCommand.isEmpty()) {
                commandManager.process(Sponge.getServer().getConsole(), lockCommand);
            }

            Task.builder()
                    .delay(settings.getGeneral().getWaitTime(), TimeUnit.SECONDS)
                    .execute(() -> attemptManager.clearAttempts(uniqueId))
                    .submit(plugin);
            return CommandResult.success();
        }

        attemptManager.increaseAttempt(uniqueId);

        //the arg isn't optional. We can be sure there is value
        String password = args.<String>getOne("password").get();

        Task.builder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new LoginTask(plugin, attemptManager, (Player) src, password))
                .name("Login Query")
                .submit(plugin);

        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(onlyOne(string(of("password"))))
                .build();
    }
}
