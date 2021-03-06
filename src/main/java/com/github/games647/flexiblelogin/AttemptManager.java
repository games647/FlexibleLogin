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
package com.github.games647.flexiblelogin;

import com.github.games647.flexiblelogin.config.Settings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@Singleton
public class AttemptManager {

    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;

    @Inject
    AttemptManager(FlexibleLogin plugin, Settings settings, EventManager eventManager) {
        maxAttempts = settings.getGeneral().getMaxAttempts();

        eventManager.registerListeners(plugin, this);
    }

    public void increaseAttempt(UUID uniqueId) {
        int old = attempts.getOrDefault(uniqueId, 0);
        attempts.put(uniqueId, old + 1);
    }

    public void clearAttempts(UUID uniqueId) {
        attempts.remove(uniqueId);
    }

    public boolean isAllowed(UUID uniqueId) {
        return attempts.computeIfAbsent(uniqueId, name -> 0) < maxAttempts;
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect disconnectEvent, @First Player player) {
        clearAttempts(player.getUniqueId());
    }
}
