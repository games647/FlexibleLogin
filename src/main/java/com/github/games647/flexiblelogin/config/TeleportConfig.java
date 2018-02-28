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
package com.github.games647.flexiblelogin.config;

import java.util.Optional;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@ConfigSerializable
public class TeleportConfig {

    @Setting
    private boolean enabled;

    @Setting(comment = "Should the plugin use the default spawn from the world you specify below")
    private boolean defaultSpawn;

    @Setting(comment = "Spawn world or let it empty to use the default world specified in the server properties")
    private String worldName = "";

    @Setting
    private int coordX;

    @Setting
    private int coordY;

    @Setting
    private int coordZ;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDefaultSpawn() {
        return defaultSpawn;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return coordX;
    }

    public int getY() {
        return coordY;
    }

    public int getZ() {
        return coordZ;
    }

    public Optional<Location<World>> getSpawnLocation() {
        if (worldName.isEmpty()) {
            worldName = Sponge.getServer().getDefaultWorldName();
        }

        return Sponge.getServer().getWorld(worldName).map(world -> {
            if (defaultSpawn) {
                return world.getSpawnLocation();
            }

            return world.getLocation(coordX, coordY, coordZ);
        });
    }
}
