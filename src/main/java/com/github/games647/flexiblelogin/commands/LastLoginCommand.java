package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Settings;
import com.google.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

public class LastLoginCommand extends AbstractCommand {

    @Inject
    LastLoginCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String username = args.<String>getOne("account").get();
        if (plugin.isValidName(username)) {
            SpongeExecutorService asyncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);
            SpongeExecutorService syncExecutor = Sponge.getScheduler().createAsyncExecutor(plugin);

            Optional<UUID> sender = Optional.empty();
            if (src instanceof Player) {
                sender = Optional.of(((Player) src).getUniqueId());
            }

            Optional<UUID> finalSender = sender;
            CompletableFuture.supplyAsync(() -> plugin.getDatabase().loadAccount(username), asyncExecutor)
                                    .thenAcceptAsync(optAcc -> onAccLoaded(finalSender, optAcc), syncExecutor);

            return CommandResult.success();
        }

        src.sendMessage(settings.getText().getInvalidUsername());
        return CommandResult.success();
    }

    private void onAccLoaded(Optional<UUID> src, Optional<Account> optAcc) {
        MessageReceiver receiver = Sponge.getServer().getConsole();
        if (src.isPresent()) {
            Optional<Player> player = Sponge.getServer().getPlayer(src.get());
            if (!player.isPresent()) {
                return;
            }

            receiver = player.get();
        }

        if (optAcc.isPresent()) {
            Account account = optAcc.get();

            String username = account.getUsername();
            String timeFormat = DateTimeFormatter.ISO_DATE_TIME.format(account.getLastLogin());
            Text message = settings.getText().getLastOnline(username, timeFormat);
            receiver.sendMessage(message);
        } else {
            receiver.sendMessage(settings.getText().getAccountNotFound());
        }
    }
}
