package com.github.games647.flexiblelogin.config;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public enum SQLType {

    MYSQL,

    SQLITE,

    H2;
}
