/*
 * The MIT License
 *
 * Copyright 2018 Toranktto.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.games647.flexiblelogin.commands.admin;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.storage.Account;
import com.google.inject.Inject;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.text.Text.of;

public class AccountsCommand extends AbstractCommand {

    @Inject
    AccountsCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<InetAddress> optIP = args.getOne("ip");
        if (optIP.isPresent()) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> {
                        Set<Account> accounts = plugin.getDatabase().getAccountsByIp(optIP.get());
                        sendAccountNames(src, optIP.get().getHostAddress(), accounts);
                    })
                    .submit(plugin);
        }

        Optional<User> optUser = args.getOne("optUser");
        if (optUser.isPresent()) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> queryAccountsByName(src, optUser.get().getName()))
                    .submit(plugin);
        }

        return CommandResult.success();
    }

    private void queryAccountsByName(CommandSource src, String username) {
        Optional<Account> optAccount = plugin.getDatabase().loadAccount(username);
        if (!optAccount.isPresent()) {
            src.sendMessage(plugin.getConfigManager().getText().getAccountNotFound());
            return;
        }

        Optional<InetAddress> optIp = optAccount.get().getIP();
        if (optIp.isPresent()) {
            Set<Account> accounts = plugin.getDatabase().getAccountsByIp(optIp.get());
            sendAccountNames(src, username, accounts);
            return;
        }

        src.sendMessage(plugin.getConfigManager().getText().getAccountsListNoIP());
    }

    private void sendAccountNames(CommandSource src, String username, Collection<Account> accounts) {
        if (accounts.isEmpty()) {
            src.sendMessage(plugin.getConfigManager().getText().getAccountsListEmpty());
            return;
        }

        Iterable<String> names = accounts.stream()
                .map(Account::getUsername)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        src.sendMessage(plugin.getConfigManager().getText().getAccountsList(username,
                String.join(", ", names)));
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(onlyOne(
                        firstParsing(
                                GenericArguments.ip(of("ip")),
                                GenericArguments.user(of("user"))))
                )
                .build();
    }
}
