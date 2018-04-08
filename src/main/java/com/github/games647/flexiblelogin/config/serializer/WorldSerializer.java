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
package com.github.games647.flexiblelogin.config.serializer;

import com.google.common.reflect.TypeToken;

import java.util.UUID;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import org.spongepowered.api.Server;
import org.spongepowered.api.world.storage.WorldProperties;

public class WorldSerializer implements TypeSerializer<WorldProperties> {

    private final Server server;

    public WorldSerializer(Server server) {
        this.server = server;
    }

    @Override
    public WorldProperties deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        String val = value.getString();
        if (val.isEmpty()) {
            return null;
        }

        try {
            UUID worldId = UUID.fromString(val);
            return server.getWorldProperties(worldId).orElse(null);
        } catch (IllegalArgumentException ex) {
            return server.getWorldProperties(val).orElse(null);
        }
    }

    @Override
    public void serialize(TypeToken<?> type, WorldProperties obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.getUniqueId());
    }
}
