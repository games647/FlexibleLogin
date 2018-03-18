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
package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.storage.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.General;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.google.inject.Inject;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent.Auth;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;

public class ConnectionListener {

    private final FlexibleLogin plugin;
    private final Settings settings;

    @Inject
    private NamePredicate namePredicate;

    @Inject
    ConnectionListener(FlexibleLogin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener
    public void onPlayerQuit(Disconnect playerQuitEvent, @First Player player) {
        Optional<Account> optAccount = plugin.getDatabase().remove(player);
        optAccount.ifPresent(account -> {
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);

            if (settings.getGeneral().isUpdateLoginStatus()) {
                Task.builder()
                        .async()
                        .execute(() -> plugin.getDatabase().save(account))
                        .submit(plugin);
            }
        });
    }

    @Listener(order = Order.FIRST)
    public void verifyPlayerName(Auth authEvent, @First GameProfile profile) {
        if (!namePredicate.test(profile.getName().get())) {
            //validate invalid characters
            authEvent.setMessage(settings.getText().getInvalidUsername());
            authEvent.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void checkAlreadyOnline(Auth authEvent, @First GameProfile profile) {
        String playerName = profile.getName().get();
        if (Sponge.getServer().getPlayer(playerName)
                .map(Player::getName)
                .filter(name -> name.equals(playerName))
                .isPresent()) {
            authEvent.setMessage(settings.getText().getAlreadyOnline());
            authEvent.setCancelled(true);
        }
    }

    @Listener(order = Order.LATE)
    public void checkCaseSensitive(Auth authEvent, @First GameProfile profile) {
        String playerName = profile.getName().get();
        if (settings.getGeneral().isCaseSensitiveNameCheck()) {
            plugin.getDatabase().exists(playerName)
                    .filter(databaseName -> !playerName.equals(databaseName))
                    .ifPresent(databaseName -> {
                        authEvent.setMessage(settings.getText().getInvalidCase(databaseName));
                        authEvent.setCancelled(true);
                    });
        }
    }

    @Listener
    public void loadAccountOnJoin(Join playerJoinEvent, @First Player player) {
        Task.builder()
                .async()
                .execute(() -> onAccountLoaded(player))
                .submit(plugin);
    }

    private void onAccountLoaded(Player player) {
        Optional<Account> optAccount = plugin.getDatabase().loadAccount(player);
        if (optAccount.isPresent()) {
            Account account = optAccount.get();
            if (canAutoLogin(account, player)) {
                //user will be auto logged in
                player.sendMessage(settings.getText().getIpAutoLogin());
                account.setLoggedIn(true);
            }
        } else if (!settings.getGeneral().isAllowUnregistered()) {
            player.kick(settings.getText().getUnregisteredKick());
            return;
        }

        scheduleTimeoutTask(player);
    }

    private boolean canAutoLogin(Account account, RemoteSource source) {
        if (!settings.getGeneral().isIpAutoLogin() || source.hasPermission(PomData.ARTIFACT_ID + ".no_auto_login")) {
            return false;
        }

        InetAddress newIp = source.getConnection().getAddress().getAddress();
        Instant now = Instant.now();
        return Objects.equals(account.getIP(), newIp) && Duration.between(account.getLastLogin(), now).toHours() < 12;
    }

    private void scheduleTimeoutTask(Player player) {
        General config = settings.getGeneral();
        if (config.isBypassed(player)) {
            return;
        }

        if (!config.isCommandOnlyProtection() && config.getTimeoutLogin() != -1) {
            Task.builder()
                    .execute(() -> {
                        if (plugin.getDatabase().getAccount(player)
                                .map(account -> !account.isLoggedIn()).orElse(false)) {
                            player.kick(settings.getText().getTimeoutReason());
                        }
                    })
                    .delay(config.getTimeoutLogin(), TimeUnit.SECONDS)
                    .submit(plugin);
        }
    }
}
