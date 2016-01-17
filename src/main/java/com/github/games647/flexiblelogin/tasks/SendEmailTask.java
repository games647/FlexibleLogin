package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfiguration;

import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.spongepowered.api.entity.living.player.Player;

public class SendEmailTask implements Runnable {

    private final FlexibleLogin plugin;
    private final Transport transport;
    private final MimeMessage email;

    private final Player player;

    public SendEmailTask(FlexibleLogin plugin, Player player, Transport transport, MimeMessage email) {
        this.plugin = plugin;
        this.transport = transport;
        this.email = email;

        this.player = player;
    }

    @Override
    public void run() {
        try {
            EmailConfiguration emailConfig = plugin.getConfigManager().getConfig().getEmailConfiguration();

            //connect to host and send message
            if (!transport.isConnected()) {
                String password = emailConfig.getPassword();
                transport.connect(emailConfig.getHost(), emailConfig.getAccount(), password);
            }

            transport.sendMessage(email, email.getAllRecipients());
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getMailSent());
        } catch (Exception ex) {
            plugin.getLogger().error("Error sending email", ex);
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getErrorCommandMessage());
        }
    }
}
