/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
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
package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfiguration;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.SendMailTask;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Provider.Type;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class ForgotPasswordCommand extends AbstractCommand {

    private static final int PASSWORD_LENGTH = 16;

    @Inject
    ForgotPasswordCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings, "forgot");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(settings.getText().getPlayersOnlyAction());
            return CommandResult.success();
        }

        checkPlayerPermission(src);

        if (!settings.getGeneral().getEmail().isEnabled()) {
            src.sendMessage(settings.getText().getEmailNotEnabled());
        }

        Player player = (Player) src;
        Optional<Account> optAccount = plugin.getDatabase().getAccount(player);
        if (optAccount.isPresent()) {
            if (optAccount.get().isLoggedIn()) {
                player.sendMessage(settings.getText().getAlreadyLoggedIn());
                return CommandResult.success();
            }
        } else {
            player.sendMessage(settings.getText().getAccountNotLoaded());
            return CommandResult.success();
        }

        Account account = optAccount.get();

        Optional<String> optEmail = account.getEmail();
        if (!optEmail.isPresent()) {
            player.sendMessage(settings.getText().getUncommittedEmailAddress());
            return CommandResult.success();
        }

        prepareSend(player, account, optEmail);
        return CommandResult.success();
    }

    private void prepareSend(Player player, Account account, Optional<String> optEmail) {
        String newPassword = generatePassword();

        EmailConfiguration emailConfig = settings.getGeneral().getEmail();
        Session session = buildSession(emailConfig);

        try {
            Message message = buildMessage(player, optEmail.get(), newPassword, emailConfig, session);

            //send email
            Task.builder()
                    .async()
                    .execute(new SendMailTask(plugin, player, session, message))
                    .submit(plugin);

            //set new password here if the email sending fails fails we have still the old password
            account.setPasswordHash(plugin.getHasher().hash(newPassword));
            Task.builder()
                    .async()
                    .execute(() -> plugin.getDatabase().save(account))
                    .submit(plugin);
        } catch (Exception ex) {
            logger.error("Error executing command", ex);
            player.sendMessage(settings.getText().getErrorCommand());
        }
    }

    private Session buildSession(EmailConfiguration emailConfig) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", emailConfig.getHost());
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", String.valueOf(emailConfig.getPort()));

        //ssl
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(emailConfig.getPort()));
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(true));
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");

        properties.setProperty("mail.transport.protocol", "flexiblelogin");

        Session session = Session.getDefaultInstance(properties);
        //explicit override stmp provider because of issues with relocation
        try {
            session.setProvider(new Provider(Type.TRANSPORT, "smtps",
                    "flexiblelogin.sun.mail.smtp.SMTPSSLTransport", "Oracle", "1.6.0"));

            session.setProvider(new Provider(Type.TRANSPORT, "flexiblelogin",
                    "flexiblelogin.sun.mail.smtp.SMTPSSLTransport", "Oracle", "1.6.0"));
        } catch (NoSuchProviderException noSuchProvider) {
            logger.error("Failed to add STMP provider", noSuchProvider);
        }

        return session;
    }

    private MimeMessage buildMessage(Player player, String email, String newPassword, EmailConfiguration emailConfig,
                                     Session session)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = new MimeMessage(session);
        String senderEmail = emailConfig.getAccount();

        //sender email with an alias
        message.setFrom(new InternetAddress(senderEmail, emailConfig.getSenderName()));
        message.setRecipient(RecipientType.TO, new InternetAddress(email, player.getName()));
        message.setSubject(replaceVariables(emailConfig.getSubject(), player, newPassword));

        //current time
        message.setSentDate(Calendar.getInstance().getTime());
        String textContent = replaceVariables(emailConfig.getText(), player, newPassword);

        //plain text
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(textContent, "text/html");

        //html part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(textContent, "text/html");

        MimeMultipart alternative = new MimeMultipart("alternative");
        alternative.addBodyPart(htmlPart);
        alternative.addBodyPart(textPart);
        message.setContent(alternative);
        return message;
    }

    private String replaceVariables(String text, Player player, String newPassword) {
        String serverName = Sponge.getServer().getBoundAddress()
                .map(inetSocketAddress -> inetSocketAddress.getAddress().getHostAddress())
                .orElse("Minecraft Server");

        return text.replace("%player%", player.getName())
                .replace("%server%", serverName)
                .replace("%password%", newPassword);
    }

    private String generatePassword() {
        return RandomStringUtils.random(PASSWORD_LENGTH);
    }
}
