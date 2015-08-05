package com.github.games647.flexiblelogin.config;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(comment = "Database configuration")
    private SQLConfiguration sqlConfiguration = new SQLConfiguration();

    @Setting(comment = "Algorithms for hashing user passwords. You can also choose totp")
    private String hashAlgo = "bcrypt";

    @Setting(comment = "Should the plugin login users automatically if it's the same account from the same IP")
    private boolean ipAutoLogin = true;

    public SQLConfiguration getSqlConfiguration() {
        return sqlConfiguration;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public boolean isIpAutoLogin() {
        return ipAutoLogin;
    }
}
