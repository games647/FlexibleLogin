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
package com.github.games647.flexiblelogin.config.nodes;

import com.google.common.collect.ImmutableMap;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import static org.spongepowered.api.text.TextTemplate.arg;
import static org.spongepowered.api.text.TextTemplate.of;

@ConfigSerializable
public class EmailConfig {

    @Setting(comment = "Is password recovery using an email allowed")
    private boolean enabled;

    @Setting(comment = "Mail server")
    private String host = "smtp.host.com";

    @Setting(comment = "SMTP Port for outgoing messages")
    private int port = 465;

    @Setting(comment = "Username for the account you want to the email from")
    private String account = "";

    @Setting(comment = "Password for the account you want to the email from")
    private String password = "";

    @Setting(comment = "Displays as sender in the email client")
    private String senderName = "Your Minecraft server name";

    @Setting(comment = "Email subject/title")
    private TextTemplate subjectTemplate = of("Your new Password on ", arg("server").optional(),
            " for ", arg("player").optional());

    @Setting(comment = "Email contents. You can use HTML here")
    private TextTemplate contentTemplate = of("New password for ", arg("player").optional(),
            " on Minecraft server ", arg("server").optional(), ": ", arg("password").optional());

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getSenderName() {
        return senderName;
    }

    public Text getSubject(String serverName, String playerName) {
        return subjectTemplate.apply(ImmutableMap.of(
                "server", serverName,
                "player", playerName
        )).build();
    }

    public Text getText(String serverName, String playerName, String password) {
        return contentTemplate.apply(ImmutableMap.of(
                "server", serverName,
                "player", playerName,
                "password", password
        )).build();
    }
}
