package com.github.games647.flexiblelogin.config.serializer;

import com.google.common.reflect.TypeToken;

import java.time.Duration;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class DurationSerializer implements TypeSerializer<Duration> {

    @Override
    public Duration deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return Duration.ofSeconds(value.getLong());
    }

    @Override
    public void serialize(TypeToken<?> type, Duration obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.getSeconds());
    }
}
