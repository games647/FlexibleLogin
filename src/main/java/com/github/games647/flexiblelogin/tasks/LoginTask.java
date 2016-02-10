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

package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;

public class LoginTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private final Player player;
    private final String userInput;

    public LoginTask(Player player, String password) {
        this.player = player;
        this.userInput = password;
    }

    @Override
    public void run() {
        Account account = plugin.getDatabase().loadAccount(player);
        if (account == null) {
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountNotFound());
            return;
        }

        try {
            if (account.checkPassword(plugin, userInput)) {
                account.setLoggedIn(true);
                //update the ip
                byte[] playerIp = player.getConnection().getAddress().getAddress().getAddress();
                account.setIp(playerIp);

                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getLoggedIn());
            } else {
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getIncorrectPassword());
            }
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error while password checking", ex);
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getErrorCommandMessage());
        }
    }
}
