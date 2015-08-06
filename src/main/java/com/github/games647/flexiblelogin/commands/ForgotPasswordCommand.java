package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfiguration;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class ForgotPasswordCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public ForgotPasswordCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "Only player need to recover their password"));
            return CommandResult.success();
        }

        Account account = plugin.getDatabase().getAccountIfPresent((Player) src);
        if (account == null) {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "You are account isn't loaded"));
            return CommandResult.success();
        } else if (account.isLoggedIn()) {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "You are already logged in"));
            return CommandResult.success();
        }

        String email = account.getEmail();
        if (email == null || email.isEmpty()) {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "You didn't submitted a email adress"));
            return CommandResult.success();
        }

        EmailConfiguration emailConfig = plugin.getConfigManager().getConfiguration().getEmailConfiguration();

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
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, src.getName()));
            message.setSubject(emailConfig.getSubject());

            //current time
            message.setSentDate(Calendar.getInstance().getTime());

            String textContent = emailConfig.getText();
            //allow html
            message.setContent(textContent, "text/html");

            //we only need to send the message so we use smtp
            Transport transport = session.getTransport("smtp");

            //connect to host and send message
            String password = emailConfig.getPassword();
            transport.connect(emailConfig.getHost(), senderEmail, password);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException ex) {
            plugin.getLogger().error("Error preparing email for password recovery", ex);
            src.sendMessage(Texts.of(TextColors.DARK_RED, "Error executing command. See console"));
        } catch (UnsupportedEncodingException ex) {
            //we can ignore this, because we will encode with UTF-8 which all Java platforms supports
        }

        return CommandResult.success();
    }
}
