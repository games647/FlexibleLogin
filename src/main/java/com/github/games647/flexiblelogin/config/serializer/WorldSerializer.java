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
