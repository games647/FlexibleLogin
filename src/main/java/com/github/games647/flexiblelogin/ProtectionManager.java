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

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.config.TeleportConfig;
import com.google.common.collect.Sets;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

@Singleton
public class ProtectionManager {

    private static final String BRIDGE_CHANNEL = "AuthMeBridge";
    private static final String LOGIN_ACTION = "PlayerLogin";
    private static final int DISTANCE = 3;

    private final Settings config;
    private final TeleportHelper teleportHelper;
    private final Map<UUID, ProtectionData> protections = new HashMap<>();

    private final RawDataChannel channel;

    @Inject
    ProtectionManager(FlexibleLogin plugin, Settings config,
                      TeleportHelper teleportHelper, ChannelRegistrar channelRegistrar) {
        this.config = config;
        this.teleportHelper = teleportHelper;

        this.channel = channelRegistrar.createRawChannel(plugin, BRIDGE_CHANNEL);
    }

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
        channel.sendTo(player, buf -> buf.writeUTF(LOGIN_ACTION));

        ProtectionData data = protections.remove(player.getUniqueId());
        if (data == null) {
            return;
        }

        if (config.getGeneral().getTeleport().isEnabled()) {
            safeTeleport(player, data.getOldLocation());
        }

        SubjectData subjectData = player.getTransientSubjectData();
        Map<Set<Context>, Map<String, Boolean>> oldPermissions = data.getPermissions();
        for (Entry<Set<Context>, Map<String, Boolean>> permission : oldPermissions.entrySet()) {
            Set<Context> context = permission.getKey();
            for (Entry<String, Boolean> perm : permission.getValue().entrySet()) {
                subjectData.setPermission(context, perm.getKey(), Tristate.fromBoolean(perm.getValue()));
            }
        }
    }

    private void safeTeleport(Player player, Location<World> location) {
        if (config.getGeneral().isSafeLocation()) {
            findPortalSafeLocation(location).ifPresent(player::setLocation);
        } else {
            player.setLocation(location);
        }
    }

    private Optional<Location<World>> findPortalSafeLocation(Location<World> location) {
        Optional<Location<World>> optSafeLoc = teleportHelper.getSafeLocation(location);
        if (!optSafeLoc.isPresent()) {
            return Optional.empty();
        }

        Location<World> safeLoc = optSafeLoc.get();

        //Sponge 7.0 adds API support for additional teleport helpers
        if (!isSafe(safeLoc)) {
            Set<Location<World>> locations = Sets.newHashSetWithExpectedSize(DISTANCE * DISTANCE);
            for (int distanceX = -DISTANCE; distanceX < DISTANCE; distanceX++) {
                for (int distanceZ = -DISTANCE; distanceZ < DISTANCE; distanceZ++) {
                    if (distanceX == 0 && distanceZ == 0) {
                        continue;
                    }

                    int newX = safeLoc.getBlockX() + distanceX;
                    int newZ = safeLoc.getBlockZ() + distanceZ;
                    Vector3d newPos = new Vector3d(newX, safeLoc.getY(), newZ);
                    locations.add(safeLoc.setPosition(newPos));
                }
            }

            return locations.stream()
                    .map(teleportHelper::getSafeLocation)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::isSafe)
                    .findFirst();
        }

        return Optional.of(safeLoc);
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
