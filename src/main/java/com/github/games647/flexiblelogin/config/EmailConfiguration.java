package com.github.games647.flexiblelogin.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class EmailConfiguration {

    @Setting(comment = "Is password recovery using an email allowed")
    private boolean enabled = false;

    @Setting(comment = "Mail server")
    private String host = "smtp.gmail.com";

    @Setting(comment = "SMTP Port for outgoing messages")
    private int port = 465;

    @Setting(comment = "Username for the account you want to the email from")
    private String account = "";

    @Setting(comment = "Password for the account you want to the email from")
    private String password = "";

    @Setting(comment = "Displays as sender in the email client")
    private String senderName = "Your minecraft server name";

    @Setting(comment = "Email subject/title")
    private String subject = "Your new Password";

    @Setting(comment = "Email contents. You can use HTML here")
    private String text = "New password for %player% on Minecraft server %server%: %password%";

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

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
}
