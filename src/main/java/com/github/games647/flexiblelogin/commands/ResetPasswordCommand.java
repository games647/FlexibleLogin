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
import com.github.games647.flexiblelogin.tasks.ResetPwTask;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class ResetPasswordCommand implements CommandExecutor {

    private static final String UUID_REGEX
            = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";
    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String accountId = args.<String>getOne("account").get();
        String password = args.<String>getOne("password").get();
        if (accountId.matches(UUID_REGEX)) {
            onUuidReset(accountId, src, password);

            return CommandResult.success();
        } else if (accountId.matches(VALID_USERNAME)) {
            onNameReset(src, accountId, password);
            return CommandResult.success();
        }

        return CommandResult.success();
    }

    private void onNameReset(CommandSource src, String accountId, String password) {
        Optional<Player> player = plugin.getGame().getServer().getPlayer(accountId);
        if (player.isPresent()) {
            Account account = plugin.getDatabase().getAccountIfPresent(player.get());
            if (account == null) {
                src.sendMessage(plugin.getConfigManager().getTextConfig().getAccountNotFound());
            } else {
                try {
                    account.setPasswordHash(plugin.getHasher().hash(password));
                    src.sendMessage(plugin.getConfigManager().getTextConfig().getChangePasswordMessage());
                } catch (Exception ex) {
                    plugin.getLogger().error("Error creating hash", ex);
                    src.sendMessage(plugin.getConfigManager().getTextConfig().getErrorCommandMessage());
                }
            }
        } else {
            //check if the account is a valid player name
            plugin.getGame().getScheduler().createTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new ResetPwTask(src, accountId, password))
                    .submit(plugin);
        }
    }

    private void onUuidReset(String accountId, CommandSource src, String password) {
        //check if the account is an UUID
        UUID uuid = UUID.fromString(accountId);
        Optional<Player> player = plugin.getGame().getServer().getPlayer(uuid);
        if (player.isPresent()) {
            Account account = plugin.getDatabase().getAccountIfPresent(player.get());
            if (account == null) {
                src.sendMessage(plugin.getConfigManager().getTextConfig().getAccountNotFound());
            } else {
                try {
                    account.setPasswordHash(plugin.getHasher().hash(password));
                    src.sendMessage(plugin.getConfigManager().getTextConfig().getChangePasswordMessage());
                } catch (Exception ex) {
                    plugin.getLogger().error("Error creating hash", ex);
                    src.sendMessage(plugin.getConfigManager().getTextConfig().getErrorCommandMessage());
                }
            }
        } else {
            plugin.getGame().getScheduler().createTaskBuilder()
                    //Async as it could run a SQL query
                    .async()
                    .execute(new ResetPwTask(src, uuid, password))
                    .submit(plugin);
        }
    }
}
