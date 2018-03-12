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
package com.github.games647.flexiblelogin.commands.admin;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.google.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.Identifiable;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class LastLoginCommand extends AbstractCommand {

    @Inject
    private NamePredicate namePredicate;

    @SynchronousExecutor
    @Inject
    private SpongeExecutorService syncExecutor;

    @AsynchronousExecutor
    @Inject
    private SpongeExecutorService asyncExecutor;

    @Inject
    LastLoginCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        String username = args.<String>getOne("account").get();
        if (namePredicate.test(username)) {
            UUID sender = null;
            if (src instanceof Identifiable) {
                sender = ((Identifiable) src).getUniqueId();
            }

            UUID finalSender = sender;
            CompletableFuture.supplyAsync(() -> plugin.getDatabase().loadAccount(username), asyncExecutor)
                    .thenAcceptAsync(optAcc -> onAccLoaded(finalSender, optAcc.orElse(null)), syncExecutor);

            return CommandResult.success();
        }

        src.sendMessage(settings.getText().getInvalidUsername());
        return CommandResult.success();
    }

    private void onAccLoaded(UUID src, Account account) {
        MessageReceiver receiver = Sponge.getServer().getConsole();
        if (src != null) {
            Optional<Player> player = Sponge.getServer().getPlayer(src);
            if (!player.isPresent()) {
                return;
            }

            receiver = player.get();
        }

        if (account == null) {
            receiver.sendMessage(settings.getText().getAccountNotFound());
        } else {
            String username = account.getUsername();
            String timeFormat = DateTimeFormatter.ISO_DATE_TIME.format(account.getLastLogin());
            Text message = settings.getText().getLastOnline(username, timeFormat);
            receiver.sendMessage(message);
        }
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(onlyOne(string(of("account"))))
                .build();
    }
}
