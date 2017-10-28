/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
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
import com.github.games647.flexiblelogin.config.SpawnTeleportConfig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

public class ProtectionManager {

    private static final int DISTANCE = 3;

    private final Settings config;
    private final TeleportHelper teleportHelper;
    private final Map<UUID, Location<World>> oldLocations = Maps.newHashMap();

    @Inject
    ProtectionManager(Settings config, TeleportHelper teleportHelper) {
        this.config = config;
        this.teleportHelper = teleportHelper;
    }

    public void protect(Player player) {
        SpawnTeleportConfig teleportConfig = config.getGeneral().getTeleport();
        if (teleportConfig.isEnabled()) {
            teleportConfig.getSpawnLocation().ifPresent(worldLocation -> {
                oldLocations.put(player.getUniqueId(), player.getLocation());
                safeTeleport(player, worldLocation);
            });
        } else {
            Location<World> oldLoc = player.getLocation();

            //sometimes players stuck in a wall
            safeTeleport(player, oldLoc);
        }
    }

    public void unprotect(Player player) {
        Location<World> oldLocation = oldLocations.remove(player.getUniqueId());
        if (oldLocation == null) {
            return;
        }

        safeTeleport(player, oldLocation);
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
        Set<Location<World>> locations = Sets.newHashSetWithExpectedSize(DISTANCE * DISTANCE);
        if (safeLoc.getBlockType() == BlockTypes.PORTAL) {
            for (int distanceX = -DISTANCE; distanceX < DISTANCE; distanceX++) {
                for (int distanceZ = -DISTANCE; distanceZ < DISTANCE; distanceZ++) {
                    if (distanceX == 0 && distanceZ == 0) {
                        continue;
                    }

                    locations.add(safeLoc.copy().add(distanceX, 0, distanceZ));
                }
            }

            return locations.stream()
                    .filter(loc -> loc.getBlockType() != BlockTypes.PORTAL)
                    .map(teleportHelper::getSafeLocation)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        }

        return Optional.of(safeLoc);
    }
}
