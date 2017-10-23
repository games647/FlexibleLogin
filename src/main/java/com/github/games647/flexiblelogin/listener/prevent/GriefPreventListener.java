package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.FlexibleLogin;

import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;

public class GriefPreventListener extends AbstractPreventListener {

    public GriefPreventListener(FlexibleLogin plugin) {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemPickup(CreateClaimEvent createClaimEvent, @Root Player player) {
        checkLoginStatus(createClaimEvent, player);
    }
}
