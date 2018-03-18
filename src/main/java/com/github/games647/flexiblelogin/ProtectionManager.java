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
import com.github.games647.flexiblelogin.config.TeleportConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.teleport.TeleportHelperFilters;

@Singleton
public class ProtectionManager {

    private static final String BRIDGE_CHANNEL = "AuthMeBridge";
    private static final String LOGIN_ACTION = "PlayerLogin";
    private static final int DISTANCE = 3;

    private final Map<UUID, ProtectionData> protections = new HashMap<>();

    @Inject
    @ChannelId(BRIDGE_CHANNEL)
    private RawDataChannel channel;

    @Inject
    private Settings config;

    @Inject
    private TeleportHelper teleportHelper;

    public void protect(Player player) {
        SubjectData subjectData = player.getTransientSubjectData();

        Map<Set<Context>, Map<String, Boolean>> permissions = Collections.emptyMap();
        if (config.getGeneral().isProtectPermissions()) {
            permissions = subjectData.getAllPermissions();
            subjectData.clearPermissions();
        }

        protections.put(player.getUniqueId(), new ProtectionData(player.getLocation(), permissions));

        TeleportConfig teleportConfig = config.getGeneral().getTeleport();
        if (teleportConfig.isEnabled()) {
            teleportConfig.getSpawnLocation().ifPresent(worldLocation -> safeTeleport(player, worldLocation));
        } else {
            Location<World> oldLoc = player.getLocation();

            //sometimes players stuck in a wall
            safeTeleport(player, oldLoc);
        }
    }

    public void unprotect(Player player) {
        //notify BungeeCord plugins for the login
        channel.sendTo(player, buf -> buf.writeUTF(LOGIN_ACTION));

        ProtectionData data = protections.remove(player.getUniqueId());
        if (data == null) {
            return;
        }

        //teleport
        if (config.getGeneral().getTeleport().isEnabled()) {
            safeTeleport(player, data.getOldLocation());
        }

        //restore permissions
        SubjectData subjectData = player.getTransientSubjectData();
        Map<Set<Context>, Map<String, Boolean>> oldPermissions = data.getPermissions();
        for (Entry<Set<Context>, Map<String, Boolean>> permission : oldPermissions.entrySet()) {
            Set<Context> context = permission.getKey();
            for (Entry<String, Boolean> perm : permission.getValue().entrySet()) {
                subjectData.setPermission(context, perm.getKey(), Tristate.fromBoolean(perm.getValue()));
            }
        }
    }

    private void safeTeleport(Entity player, Location<World> loc) {
        Location<World> safeLoc = loc;
        if (config.getGeneral().isSafeLocation()) {
            Optional<Location<World>> optSafe = teleportHelper.getSafeLocation(loc,
                    DISTANCE, DISTANCE, 0, TeleportHelperFilters.NO_PORTAL);
            if (optSafe.isPresent()) {
                safeLoc = optSafe.get();
            }
        }

        player.setLocation(safeLoc);
    }

    private boolean isSafe(Location<World> loc) {
        return loc.getBlockType() != BlockTypes.PORTAL;
    }

    @Listener
    public void onPlayerQuit(Disconnect playerQuitEvent, @First Player player) {
        unprotect(player);
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(Join playerJoinEvent, @First Player player) {
        protect(player);
    }

    @Listener
    public void onDisable(GameStoppingServerEvent stoppingEvent) {
        Sponge.getServer().getOnlinePlayers().forEach(this::unprotect);
    }

    private class ProtectionData {

        private final Location<World> oldLocation;
        private final Map<Set<Context>, Map<String, Boolean>> permissions;

        public ProtectionData(Location<World> oldLocation, Map<Set<Context>, Map<String, Boolean>> permissions) {
            this.oldLocation = oldLocation;
            this.permissions = permissions;
        }

        public Location<World> getOldLocation() {
            return oldLocation;
        }

        public Map<Set<Context>, Map<String, Boolean>> getPermissions() {
            return permissions;
        }
    }
}
