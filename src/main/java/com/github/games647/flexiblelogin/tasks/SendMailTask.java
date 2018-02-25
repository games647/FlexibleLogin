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
package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfig;

import java.util.Arrays;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.spongepowered.api.entity.living.player.Player;

public class SendMailTask implements Runnable {

    private final FlexibleLogin plugin;
    private final Session session;
    private final Message email;

    private final Player player;

    public SendMailTask(FlexibleLogin plugin, Player player, Session session, Message email) {
        this.plugin = plugin;
        this.session = session;
        this.email = email;
        this.player = player;
    }

    @Override
    public void run() {
        //we only need to send the message so we use smtps
        try (Transport transport = session.getTransport()) {
            EmailConfig emailConfig = plugin.getConfigManager().getGeneral().getEmail();

            //connect to host and send message
            if (!transport.isConnected()) {
                String password = emailConfig.getPassword();
                transport.connect(emailConfig.getHost(), emailConfig.getAccount(), password);
            }

            transport.sendMessage(email, email.getAllRecipients());
            player.sendMessage(plugin.getConfigManager().getText().getMailSent());
        } catch (NoSuchProviderException providerEx) {
            plugin.getLogger().error("Transport provider not found", providerEx);
            plugin.getLogger().error("Registered providers: {}", Arrays.asList(session.getProviders()));

            player.sendMessage(plugin.getConfigManager().getText().getErrorCommand());
        } catch (MessagingException messagingEx) {
            plugin.getLogger().error("Error sending email", messagingEx);
            player.sendMessage(plugin.getConfigManager().getText().getErrorCommand());
        }
    }
}
