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

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.tasks.SaveTask;

import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class SetEmailCommand implements CommandExecutor {

    private final Pattern emailPattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(plugin.getConfigManager().getText().getPlayersOnlyAction());
            return CommandResult.empty();
        }

        plugin.checkPlayerPermission(src, "email");

        String email = args.<String>getOne("email").get();
        if (emailPattern.matcher(email).matches()) {
            Account account = plugin.getDatabase().getAccountIfPresent((Player) src);
            if (account != null) {
                account.setEmail(email);
                src.sendMessage(plugin.getConfigManager().getText().getEmailSet());
                Task.builder()
                        .async()
                        .execute(new SaveTask(account))
                        .submit(plugin);
            }

            return CommandResult.success();
        }

        src.sendMessage(plugin.getConfigManager().getText().getNotEmail());
        return CommandResult.success();
    }
}
