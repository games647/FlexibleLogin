/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
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

    @Setting(comment = "Number of seconds a player has time to login or will be kicked.-1 deactivates this features")
    private int timeoutLogin = 60;

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

    public int getTimeoutLogin() {
        return timeoutLogin;
    }

    public List<String> getProtectedCommands() {
        return protectedCommands;
    }
}
