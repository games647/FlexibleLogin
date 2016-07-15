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
package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Config;
import com.github.games647.flexiblelogin.tasks.LoginMessageTask;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class ConnectionListener {

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect playerQuitEvent) {
        Player player = playerQuitEvent.getTargetEntity();
        Account account = plugin.getDatabase().getAccountIfPresent(player);

        plugin.getProtectionManager().unprotect(player);

        if (account != null) {
            plugin.getAttempts().remove(player.getConnection());
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);

            if (plugin.getConfigManager().getConfig().isUpdateLoginStatus()) {
                plugin.getGame().getScheduler().createTaskBuilder()
                        .async().execute(() -> plugin.getDatabase().flushLoginStatus(account, false))
                        .submit(plugin);
            }
        }
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join playerJoinEvent) {
        Player player = playerJoinEvent.getTargetEntity();
        if (!player.getName().matches(VALID_USERNAME)) {
            //validate invalid characters
            player.kick(plugin.getConfigManager().getTextConfig().getInvalidUsername());
            playerJoinEvent.setMessage(Text.EMPTY);
        }

        plugin.getProtectionManager().protect(player);

        plugin.getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(() -> onAccountLoaded(player))
                .submit(plugin);
    }

    private void onAccountLoaded(Player player) {
        Account loadedAccount = plugin.getDatabase().loadAccount(player);
        byte[] newIp = player.getConnection().getAddress().getAddress().getAddress();

        Config config = plugin.getConfigManager().getConfig();
        if (loadedAccount == null) {
            if (config.isCommandOnlyProtection()) {
                if (player.hasPermission(plugin.getContainer().getId() + ".registerRequired")) {
                    //command only protection but have to register
                    sendNotLoggedInMessage(player);
                }
            } else {
                //no account
                sendNotLoggedInMessage(player);
            }
        } else if (config.isIpAutoLogin() && Arrays.equals(loadedAccount.getIp(), newIp)) {
            //user will be auto logged in
            player.sendMessage(plugin.getConfigManager().getTextConfig().getIpAutoLogin());
            loadedAccount.setLoggedIn(true);
        } else {
            //user has an account but isn't logged in
            sendNotLoggedInMessage(player);
        }

        scheduleTimeoutTask(player);
    }

    private void sendNotLoggedInMessage(Player player) {
        //send the message if the player only needs to login
        if (!plugin.getConfigManager().getConfig().isBypassPermission()
                || !player.hasPermission(plugin.getContainer().getId() + ".bypass")) {
            plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(new LoginMessageTask(player))
                    .interval(2, TimeUnit.SECONDS).submit(plugin);
        }
    }

    private void scheduleTimeoutTask(Player player) {
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        Config config = plugin.getConfigManager().getConfig();
        if (!config.isCommandOnlyProtection() && config.getTimeoutLogin() != -1
                && account != null && !account.isLoggedIn()) {
            plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(() -> {
                        if (!account.isLoggedIn()) {
                            player.kick(plugin.getConfigManager().getTextConfig().getTimeoutReason());
                        }
                    })
                    .delay(config.getTimeoutLogin(), TimeUnit.SECONDS)
                    .submit(plugin);
        }
    }
}
