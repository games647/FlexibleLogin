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
import com.github.games647.flexiblelogin.tasks.SaveTask;
import com.github.games647.flexiblelogin.tasks.SendEmailTask;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ForgotPasswordCommand implements CommandExecutor {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(plugin.getConfigManager().getTextConfig().getPlayersOnlyActionMessage());
            return CommandResult.success();
        }

        if (plugin.getConfigManager().getConfig().isPlayerPermissions()
                && !src.hasPermission(plugin.getContainer().getId() + ".command.forgot")) {
            throw new CommandPermissionException();
        }

        Player player = (Player) src;
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account == null) {
            src.sendMessage(plugin.getConfigManager().getTextConfig().getAccountNotLoadedMessage());
            return CommandResult.success();
        } else if (account.isLoggedIn()) {
            src.sendMessage(plugin.getConfigManager().getTextConfig().getAlreadyLoggedInMessage());
            return CommandResult.success();
        }

        String email = account.getEmail();
        if (email == null || email.isEmpty()) {
            src.sendMessage(plugin.getConfigManager().getTextConfig().getUncommittedEmailAddressMessage());
            return CommandResult.success();
        }

        String newPassword = generatePassword();

        EmailConfiguration emailConfig = plugin.getConfigManager().getConfig().getEmailConfiguration();

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", emailConfig.getHost());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", emailConfig.getPort());
        properties.put("mail.smtp.starttls.enable", true);

        Session session = Session.getDefaultInstance(properties);

        //prepare email
        MimeMessage message = new MimeMessage(session);
        try {
            String senderEmail = emailConfig.getAccount();
            //sender email with an alias
            message.setFrom(new InternetAddress(senderEmail, emailConfig.getSenderName()));
            message.setRecipient(RecipientType.TO, new InternetAddress(email, src.getName()));
            message.setSubject(replaceVariables(emailConfig.getSubject(), player, newPassword));

            //current time
            message.setSentDate(Calendar.getInstance().getTime());

            String textContent = replaceVariables(emailConfig.getText(), player, newPassword);
            //allow html
            message.setContent(textContent, "text/html");

            //we only need to send the message so we use smtp
            Transport transport = session.getTransport("smtp");
            //send email
            plugin.getGame().getScheduler().createTaskBuilder()
                    .async()
                    .execute(new SendEmailTask(player, transport, message))
                    .submit(plugin);

            //set new password here if the email sending fails fails we have still the old password
            account.setPasswordHash(plugin.getHasher().hash(newPassword));
            plugin.getGame().getScheduler().createTaskBuilder()
                    .async()
                    .execute(new SaveTask(account))
                    .submit(plugin);
        } catch (UnsupportedEncodingException ex) {
            //we can ignore this, because we will encode with UTF-8 which all Java platforms supports
        } catch (Exception ex) {
            plugin.getLogger().error("Error executing command", ex);
            src.sendMessage(Text.of(TextColors.DARK_RED
                    , plugin.getConfigManager().getTextConfig().getErrorCommandMessage()));
        }

        return CommandResult.success();
    }

    private String replaceVariables(String text, Player player, String newPassword) {
        String serverName = plugin.getGame().getServer().getBoundAddress().get().getAddress().getHostAddress();
        return text.replace("%player%", player.getName())
                .replace("%server%", serverName).replace("%password%", newPassword);
    }

    private String generatePassword() {
        return RandomStringUtils.random(8);
    }
}
