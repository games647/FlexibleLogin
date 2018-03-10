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

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.Settings;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

public class MessageTask implements Runnable {

    private final FlexibleLogin plugin;
    private final Settings settings;

    public MessageTask(FlexibleLogin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void run() {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                //send the message if the player only needs to login
                .filter(player -> !settings.getGeneral().isBypassed(player))
                .filter(this::isRegistrationRequired)
                .forEach(this::sendMessage);
    }

    private void sendMessage(Player player) {
        Optional<Account> optAccount = plugin.getDatabase().getAccount(player);
        if (optAccount.isPresent()) {
            Account account = optAccount.get();
            if (!account.isLoggedIn()) {
                player.sendMessage(settings.getText().getNotLoggedIn());
            }
        } else {
            player.sendMessage(settings.getText().getNotRegistered());
        }
    }

    private boolean isRegistrationRequired(Subject player) {
        return !settings.getGeneral().isCommandOnlyProtection()
                || player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired");
    }
}
