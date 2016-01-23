package com.github.games647.flexiblelogin.config;

import com.google.common.collect.Lists;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(comment = "Database configuration")
    private SQLConfiguration sqlConfiguration = new SQLConfiguration();

    @Setting(comment = "Email configuration for password recovery")
    private EmailConfiguration emailConfiguration = new EmailConfiguration();

    @Setting(comment = "Text configuration for custom messages in chat")
    private TextConfiguration textConfiguration = new TextConfiguration();

    @Setting(comment = "Algorithms for hashing user passwords. You can also choose totp")
    private String hashAlgo = "bcrypt";

    @Setting(comment = "Should the plugin login users automatically if it's the same account from the same IP")
    private boolean ipAutoLogin = false;

    @Setting(comment = "Should only the specified commands be protected from unauthorized access")
    private boolean commandOnlyProtection = false;

    @Setting(comment = "If command only protection is enabled, these commands are protected. If the list is empty"
            + " all commands are protected")
    private List<String> protectedCommands = Lists.newArrayList("op", "pex");

    public EmailConfiguration getEmailConfiguration() {
        return emailConfiguration;
    }

    public SQLConfiguration getSqlConfiguration() {
        return sqlConfiguration;
    }

    public TextConfiguration getTextConfig() {
        return textConfiguration;
    }

    public String getHashAlgo() {
        return hashAlgo;
    }

    public boolean isIpAutoLogin() {
        return ipAutoLogin;
    }

    public boolean isCommandOnlyProtection() {
        return commandOnlyProtection;
    }

    public List<String> getProtectedCommands() {
        return protectedCommands;
    }
}
