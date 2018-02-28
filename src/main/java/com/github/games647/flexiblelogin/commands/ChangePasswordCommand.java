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

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Settings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class ChangePasswordCommand extends AbstractCommand {

    @Inject
    ChangePasswordCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings, "changepw");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(settings.getText().getPlayersOnly());
            return CommandResult.empty();
        }

        checkPlayerPermission(src);

        if (!plugin.getDatabase().isLoggedIn((Player) src)) {
            src.sendMessage(settings.getText().getNotLoggedIn());
            return CommandResult.empty();
        }

        Account account = plugin.getDatabase().getAccount((Player) src).get();

        Collection<String> passwords = args.getAll("password");
        List<String> indexPasswords = Lists.newArrayList(passwords);
        String password = indexPasswords.get(0);
        if (password.equals(indexPasswords.get(1))) {
            try {
                //Check if the first two passwords are equal to prevent typos
                String hash = plugin.getHasher().hash(password);
                Task.builder()
                        //we are executing a SQL Query which is blocking
                        .async()
                        .execute(() -> {
                            account.setPasswordHash(hash);
                            boolean success = plugin.getDatabase().save(account);
                            if (success) {
                                src.sendMessage(settings.getText().getChangePassword());
                            } else {
                                src.sendMessage(settings.getText().getErrorExecutingCommand());
                            }
                        })
                        .name("Register Query")
                        .submit(plugin);
            } catch (Exception ex) {
                logger.error("Error creating hash on change password", ex);
                src.sendMessage(settings.getText().getErrorExecutingCommand());
            }
        } else {
            src.sendMessage(settings.getText().getUnequalPasswords());
        }

        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(GenericArguments
                        .repeated(
                                string(of("password")), 2))
                .build();
    }
}
