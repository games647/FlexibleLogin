/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
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

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.UnregisterTask;

import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class UnregisterCommand implements CommandExecutor {

    private static final String UUID_REGEX
            = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";
    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String account = args.<String>getOne("account").get();
        if (account.matches(UUID_REGEX)) {
            //check if the account is an UUID
            UUID uuid = UUID.fromString(account);
            plugin.getGame().getScheduler().createTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new UnregisterTask(src, uuid))
                    .submit(plugin);
            return CommandResult.success();
        } else if (account.matches(VALID_USERNAME)) {
            //check if the account is a valid player name
            plugin.getGame().getScheduler().createTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new UnregisterTask(src, account))
                    .submit(plugin);
            return CommandResult.success();
        }

        src.sendMessage(plugin.getConfigManager().getTextConfig().getUnregisteringFailedMessage());

        return CommandResult.success();
    }
}
