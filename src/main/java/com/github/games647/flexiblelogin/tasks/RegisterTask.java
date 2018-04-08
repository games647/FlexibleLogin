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
package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.config.nodes.General.HashingAlgorithm;
import com.github.games647.flexiblelogin.hasher.TOTP;
import com.github.games647.flexiblelogin.storage.Account;
import com.google.common.base.Splitter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.text.action.TextActions.openUrl;

public class RegisterTask implements Runnable {

    private final FlexibleLogin plugin;
    private final ProtectionManager protectionManager;

    private final Player player;
    private final String password;

    public RegisterTask(FlexibleLogin plugin, ProtectionManager protectionManager, Player player) {
        this(plugin, protectionManager, player, "");
    }

    public RegisterTask(FlexibleLogin plugin, ProtectionManager protectionManager, Player player, String password) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
        this.player = player;
        this.password = password;
    }

    @Override
    public void run() {
        String name = player.getName();
        if (!plugin.getDatabase().loadAccount(player).isPresent()) {
            int regByIp = plugin.getDatabase().getRegistrationsCount(player.getConnection().getAddress().getAddress());
            if (regByIp > plugin.getConfigManager().getGeneral().getMaxIpReg()) {
                player.sendMessage(plugin.getConfigManager().getText().getMaxIpReg());
                return;
            }

            try {
                String hashedPassword = plugin.getHasher().hash(password);
                Account createdAccount = new Account(player, hashedPassword);
                if (!plugin.getDatabase().createAccount(createdAccount)) {
                    return;
                }

                plugin.getDatabase().addCache(player.getUniqueId(), createdAccount);

                //thread-safe, because it's immutable after config load
                if (plugin.getConfigManager().getGeneral().getHashAlgo() == HashingAlgorithm.TOTP) {
                    sendTotpHint(hashedPassword);
                }

                player.sendMessage(plugin.getConfigManager().getText().getAccountCreated());
                createdAccount.setLoggedIn(true);
                if (plugin.getConfigManager().getGeneral().isUpdateLoginStatus()) {
                    plugin.getDatabase().save(createdAccount);
                }

                Task.builder()
                        .execute(() -> protectionManager.unprotect(player))
                        .submit(plugin);
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                player.sendMessage(plugin.getConfigManager().getText().getErrorExecutingCommand());
            }
        } else {
            player.sendMessage(plugin.getConfigManager().getText().getAccountAlreadyExists());
        }
    }

    private void sendTotpHint(String secretCode) {
        //I assume this thread-safe, because PlayerChat is also in an async task
        String hostName = Sponge.getServer().getBoundAddress()
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getCanonicalHostName)
                .orElse("Minecraft Server");
        try {
            TOTP hasher = (TOTP) plugin.getHasher();

            URL barcodeUrl = new URL(hasher.getGoogleBarcodeURL(player.getName(), hostName, secretCode));
            String readableSecret = Splitter.fixedLength(4).splitToList(secretCode).stream()
                    .collect(Collectors.joining(" "));

            Text keyGenerated = plugin.getConfigManager().getText().getKeyGenerated(readableSecret);
            player.sendMessage(keyGenerated);
            player.sendMessage(plugin.getConfigManager().getText().getScanQr().toBuilder()
                            .onClick(openUrl(barcodeUrl))
                            .build());
        } catch (MalformedURLException ex) {
            plugin.getLogger().error("Malformed TOTP url link", ex);
        }
    }
}
