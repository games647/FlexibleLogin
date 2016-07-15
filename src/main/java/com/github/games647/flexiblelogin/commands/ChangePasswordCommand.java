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
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class ChangePasswordCommand implements CommandExecutor {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(plugin.getConfigManager().getTextConfig().getPlayersOnlyActionMessage());
            return CommandResult.empty();
        }

        if (plugin.getConfigManager().getConfig().isPlayerPermissions()
                && !source.hasPermission(plugin.getContainer().getId() + ".command.changepw")) {
            throw new CommandPermissionException();
        }

        Account account = plugin.getDatabase().getAccountIfPresent((Player) source);
        if (account == null || !account.isLoggedIn()) {
            source.sendMessage(plugin.getConfigManager().getTextConfig().getNotLoggedInMessage());
            return CommandResult.empty();
        }

        Collection<String> passwords = args.<String>getAll("password");
        List<String> indexPasswords = Lists.newArrayList(passwords);
        String password = indexPasswords.get(0);
        if (password.equals(indexPasswords.get(1))) {
            try {
                //Check if the first two passwords are equal to prevent typos
                String hash = plugin.getHasher().hash(password);
                plugin.getGame().getScheduler().createTaskBuilder()
                        //we are executing a SQL Query which is blocking
                        .async()
                        .execute(() -> {
                            account.setPasswordHash(hash);
                            boolean success = plugin.getDatabase().save(account);
                            if (success) {
                                source.sendMessage(plugin.getConfigManager().getTextConfig().getChangePasswordMessage());
                            } else {
                                source.sendMessage(plugin.getConfigManager().getTextConfig().getErrorCommandMessage());
                            }
                        })
                        .name("Register Query")
                        .submit(plugin);
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash on change password", ex);
                source.sendMessage(plugin.getConfigManager().getTextConfig().getErrorCommandMessage());
            }
        } else {
            source.sendMessage(plugin.getConfigManager().getTextConfig().getUnequalPasswordsMessage());
        }

        return CommandResult.success();
    }
}
