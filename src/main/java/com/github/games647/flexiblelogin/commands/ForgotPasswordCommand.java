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
package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfiguration;
import com.github.games647.flexiblelogin.tasks.SendEmailTask;
import com.google.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class ForgotPasswordCommand extends AbstractCommand {

    @Inject
    ForgotPasswordCommand(FlexibleLogin plugin) {
        super(plugin, "forgot");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(plugin.getConfigManager().getText().getPlayersOnlyAction());
            return CommandResult.success();
        }

        checkPlayerPermission(src);

        if (!plugin.getConfigManager().getGeneral().getEmail().isEnabled()) {
            src.sendMessage(plugin.getConfigManager().getText().getEmailNotEnabled());
        }

        Player player = (Player) src;
        Optional<Account> optAccount = plugin.getDatabase().getAccount(player);
        if (optAccount.isPresent()) {
            if (optAccount.get().isLoggedIn()) {
                src.sendMessage(plugin.getConfigManager().getText().getAlreadyLoggedIn());
                return CommandResult.success();
            }
        } else {
            src.sendMessage(plugin.getConfigManager().getText().getAccountNotLoaded());
            return CommandResult.success();
        }

        Account account = optAccount.get();

        Optional<String> optEmail = account.getEmail();
        if (!optEmail.isPresent()) {
            src.sendMessage(plugin.getConfigManager().getText().getUncommittedEmailAddress());
            return CommandResult.success();
        }

        String newPassword = generatePassword();

        EmailConfiguration emailConfig = plugin.getConfigManager().getGeneral().getEmail();

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

        Session session = Session.getDefaultInstance(properties);

        //prepare email
        MimeMessage message = new MimeMessage(session);
        try {
            String senderEmail = emailConfig.getAccount();
            //sender email with an alias
            message.setFrom(new InternetAddress(senderEmail, emailConfig.getSenderName()));
            message.setRecipient(RecipientType.TO, new InternetAddress(optEmail.get(), src.getName()));
            message.setSubject(replaceVariables(emailConfig.getSubject(), player, newPassword));

            //current time
            message.setSentDate(Calendar.getInstance().getTime());

            String textContent = replaceVariables(emailConfig.getText(), player, newPassword);
            //allow html
            message.setContent(textContent, "text/html");

            //send email
            Task.builder()
                    .async()
                    .execute(new SendEmailTask(plugin, player, session, message))
                    .submit(plugin);

            //set new password here if the email sending fails fails we have still the old password
            account.setPasswordHash(plugin.getHasher().hash(newPassword));
            Task.builder()
                    .async()
                    .execute(() -> plugin.getDatabase().save(account))
                    .submit(plugin);
        } catch (UnsupportedEncodingException ex) {
            //we can ignore this, because we will encode with UTF-8 which all Java platforms supports
        } catch (Exception ex) {
            plugin.getLogger().error("Error executing command", ex);
            src.sendMessage(plugin.getConfigManager().getText().getErrorCommand());
        }

        return CommandResult.success();
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
        return RandomStringUtils.random(8);
    }
}
