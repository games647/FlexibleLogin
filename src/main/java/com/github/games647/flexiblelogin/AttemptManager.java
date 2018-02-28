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

    public int increaseAttempt(UUID uniqueId) {
        return attempts.computeIfAbsent(uniqueId, name -> 0);
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
