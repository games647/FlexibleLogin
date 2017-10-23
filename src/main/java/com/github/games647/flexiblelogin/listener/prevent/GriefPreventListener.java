package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Settings;
import com.google.inject.Inject;

import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;

public class GriefPreventListener extends AbstractPreventListener {

    @Inject
    GriefPreventListener(FlexibleLogin plugin, Settings settings) {
        super(plugin, settings);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemPickup(CreateClaimEvent createClaimEvent, @Root Player player) {
        checkLoginStatus(createClaimEvent, player);
    }
}
