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
package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.Settings;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;

public abstract class AbstractPreventListener {

    protected final FlexibleLogin plugin;
    protected final Settings settings;

    public AbstractPreventListener(FlexibleLogin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    protected void checkLoginStatus(Cancellable event, Player player) {
        if (!isAllowed(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isAllowed(Player player) {
        if (settings.getGeneral().isBypassed(player)) {
            return true;
        }

        if (settings.getGeneral().isCommandOnlyProtection()) {
            //check if the user is already registered
            return plugin.getDatabase().getAccount(player).isPresent()
                    || !player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired");
        }

        return plugin.getDatabase().isLoggedIn(player);
    }
}
