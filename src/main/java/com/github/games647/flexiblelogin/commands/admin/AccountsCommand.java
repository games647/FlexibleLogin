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
import com.github.games647.flexiblelogin.validation.IPv4Predicate;
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.github.games647.flexiblelogin.validation.UUIDPredicate;
import com.google.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import static org.spongepowered.api.text.Text.of;

public class AccountsCommand extends AbstractCommand {

    @Inject
    private UUIDPredicate uuidPredicate;

    @Inject
    private NamePredicate namePredicate;

    @Inject
    private IPv4Predicate ipv4Predicate;

    @Inject
    AccountsCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String accountId = args.<String>getOne("account").get();

        if (ipv4Predicate.test(accountId)) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> {
                        InetAddress ip = null;
                        try {
                            ip = InetAddress.getByName(accountId);
                        } catch (UnknownHostException ex) {
                            src.sendMessage(plugin.getConfigManager().getText().getInvalidIPv4());
                            return;
                        }
                        
                        Set<Account> accounts = plugin.getDatabase().getAccountsByIp(ip);
                        sendAccountsList(src, accountId, accounts);
                    })
                    .submit(plugin);
            return CommandResult.success();
        } else if (namePredicate.test(accountId)) {
            Task.builder()
                    //we are executing a SQL Query which is blocking
                    .async()
                    .execute(() -> {
                        Optional<Account> optAccount = plugin.getDatabase().loadAccount(accountId);
                        if(!optAccount.isPresent()) {
                            src.sendMessage(plugin.getConfigManager().getText().getAccountNotFound());
                            return;
                        }
                        
                        InetAddress ip = optAccount.get().getIP();
                        
                        if(ip == null) {
                            src.sendMessage(plugin.getConfigManager().getText().getAccountsListNoIP());
                            return;
                        }
                        
                        Set<Account> accounts = plugin.getDatabase().getAccountsByIp(ip);
                        
                        sendAccountsList(src, accountId, accounts);
                    })
                    .submit(plugin);
            return CommandResult.success();
        }

        src.sendMessage(settings.getText().getInvalidUsername());
        return CommandResult.success();
    }

    private void sendAccountsList(CommandSource src, String username, Set<Account> accounts) {
        if(accounts.isEmpty()) {
            src.sendMessage(plugin.getConfigManager().getText().getAccountsListEmpty());
            return;
        }
        
        List<String> accountsList = new ArrayList<>();
        
        for(Account account : accounts) {
            Optional<String> optAccountName = account.getUsername();
            if(!optAccountName.isPresent()) {
                continue;
            }
            
            String accountName = optAccountName.get();
            
            accountsList.add(accountName);
        }
        
        src.sendMessage(plugin.getConfigManager().getText().getAccountsList(username,
                String.join(", ", accountsList)));
    }
    
    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(onlyOne(string(of("account"))))
                .build();
    }
}
