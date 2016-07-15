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

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;

public class ResetPwTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private final CommandSource src;
    private final Object accountIndentifer;
    private final String password;

    public ResetPwTask(CommandSource src, UUID uuid, String password) {
        this.src = src;
        this.accountIndentifer = uuid;
        this.password = password;
    }

    public ResetPwTask(CommandSource src, String playerName, String password) {
        this.src = src;
        this.accountIndentifer = playerName;
        this.password = password;
    }

    @Override
    public void run() {
        Account account = null;
        if (accountIndentifer instanceof String) {
            account = plugin.getDatabase().loadAccount((String) accountIndentifer);
        } else {
            account = plugin.getDatabase().loadAccount((UUID) accountIndentifer);
        }

        if (account == null) {
            src.sendMessage(plugin.getConfigManager().getTextConfig().getAccountNotFound());
        } else {
            try {
                account.setPasswordHash(plugin.getHasher().hash(password));
                src.sendMessage(plugin.getConfigManager().getTextConfig().getChangePasswordMessage());
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                src.sendMessage(plugin.getConfigManager().getTextConfig().getErrorCommandMessage());
            }
        }
    }
}
