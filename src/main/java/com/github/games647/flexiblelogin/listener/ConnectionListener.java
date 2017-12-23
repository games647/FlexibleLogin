/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
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

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.config.Config;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.LoginMessageTask;
import com.google.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent.Auth;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;

public class ConnectionListener {

    private final FlexibleLogin plugin;
    private final Settings settings;
    private final ProtectionManager protectionManager;

    @Inject
    public ConnectionListener(FlexibleLogin plugin, Settings settings, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.settings = settings;
        this.protectionManager = protectionManager;
    }

    @Listener
    public void onPlayerQuit(Disconnect playerQuitEvent, @First Player player) {
        Account account = plugin.getDatabase().remove(player);

        protectionManager.unprotect(player);

        if (account != null) {
            plugin.getAttempts().remove(player.getName());
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);

            if (settings.getGeneral().isUpdateLoginStatus()) {
                Task.builder()
                        .async().execute(() -> plugin.getDatabase().save(account))
                        .submit(plugin);
            }
        }
    }

    @Listener
    public void onPlayerJoin(Join playerJoinEvent, @First Player player) {
        protectionManager.protect(player);
        Task.builder()
                .async()
                .execute(() -> onAccountLoaded(player))
                .submit(plugin);
    }

    @Listener
    public void onPlayerAuth(Auth playerAuthEvent, @First GameProfile gameProfile) {
        String playerName = gameProfile.getName().get();
        if (plugin.isValidName(playerName)) {
            if (Sponge.getServer().getPlayer(playerName)
                    .map(Player::getName)
                    .filter(name -> name.equals(playerName))
                    .isPresent()) {
                playerAuthEvent.setMessage(settings.getText().getAlreadyOnline());
                playerAuthEvent.setCancelled(true);
                return;
            }

            plugin.getDatabase().exists(playerName)
                    .filter(databaseName -> !playerName.equals(databaseName))
                    .ifPresent(databaseName -> {
                        playerAuthEvent.setMessage(settings.getText().getInvalidCase(databaseName));
                        playerAuthEvent.setCancelled(true);
                    });
        } else {
            //validate invalid characters
            playerAuthEvent.setMessage(settings.getText().getInvalidUsername());
            playerAuthEvent.setCancelled(true);
        }
    }

    private void onAccountLoaded(Player player) {
        Optional<Account> optAccount = plugin.getDatabase().loadAccount(player);
        byte[] newIp = player.getConnection().getAddress().getAddress().getAddress();

        Config config = settings.getGeneral();
        if (optAccount.isPresent()) {
            Account account = optAccount.get();

            Instant lastLogin = account.getLastLogin();
            if (config.isIpAutoLogin() && Arrays.equals(account.getIp(), newIp)
                    && Duration.between(lastLogin, Instant.now()).getSeconds() > TimeUnit.HOURS.toSeconds(12)
                    && !player.hasPermission(PomData.ARTIFACT_ID + ".no_auto_login")) {
                //user will be auto logged in
                player.sendMessage(settings.getText().getIpAutoLogin());
                account.setLoggedIn(true);
            } else {
                //user has an account but isn't logged in
                sendNotLoggedInMessage(player);
            }
        } else {
            if (config.isCommandOnlyProtection()) {
                if (player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired")) {
                    //command only protection but have to register
                    sendNotLoggedInMessage(player);
                }
            } else {
                //no account
                sendNotLoggedInMessage(player);
            }
        }

        scheduleTimeoutTask(player);
    }

    private void sendNotLoggedInMessage(Player player) {
        if (settings.getGeneral().isBypassPermission() && player.hasPermission(PomData.ARTIFACT_ID + ".bypass")) {
            //send the message if the player only needs to login
            return;
        }

        Task.builder()
                .execute(new LoginMessageTask(plugin, player))
                .interval(settings.getGeneral().getMessageInterval(), TimeUnit.SECONDS)
                .submit(plugin);
    }

    private void scheduleTimeoutTask(Player player) {
        Config config = settings.getGeneral();
        if (config.isBypassPermission() && player.hasPermission(PomData.ARTIFACT_ID + ".bypass")) {
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
