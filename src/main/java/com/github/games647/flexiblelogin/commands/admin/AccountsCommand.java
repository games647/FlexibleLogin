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
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.github.games647.flexiblelogin.validation.UUIDPredicate;
import com.google.common.net.InetAddresses;
import com.google.inject.Inject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class AccountsCommand extends AbstractCommand {

    @Inject
    private UUIDPredicate uuidPredicate;

    @Inject
    private NamePredicate namePredicate;

    @Inject
    AccountsCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String accountId = args.<String>getOne("account").get();

        if (InetAddresses.isInetAddress(accountId)) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> queryAccountsByIP(src, accountId))
                    .submit(plugin);
            return CommandResult.success();
        } else if (namePredicate.test(accountId)) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> queryAccountsByName(src, accountId))
                    .submit(plugin);
            return CommandResult.success();
        }

        src.sendMessage(settings.getText().getInvalidUsername());
        return CommandResult.success();
    }

    private void queryAccountsByIP(CommandSource src, String ipString) {
        InetAddress ip;

        try {
            ip = InetAddress.getByName(ipString);
        } catch (UnknownHostException ex) {
            src.sendMessage(plugin.getConfigManager().getText().getInvalidIP());
            return;
        }

        Set<Account> accounts = plugin.getDatabase().getAccountsByIp(ip);
        sendAccountNames(src, ipString, accounts);
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

    private void sendAccountNames(CommandSource src, String username, Set<Account> accounts) {
        if (accounts.isEmpty()) {
            src.sendMessage(plugin.getConfigManager().getText().getAccountsListEmpty());
            return;
        }

        List<String> names = accounts.stream()
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
                .arguments(onlyOne(string(of("account"))))
                .build();
    }
}
