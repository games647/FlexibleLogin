package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.EmailConfiguration;
import com.github.games647.flexiblelogin.tasks.SaveTask;
import com.github.games647.flexiblelogin.tasks.SendEmailTask;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ForgotPasswordCommand implements CommandExecutor {

    private final FlexibleLogin plugin;

    public ForgotPasswordCommand(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getPlayersOnlyActionMessage());
            return CommandResult.success();
        }

        Player player = (Player) src;
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account == null) {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountNotLoadedMessage());
            return CommandResult.success();
        } else if (account.isLoggedIn()) {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAlreadyLoggedInMessage());
            return CommandResult.success();
        }

        String email = account.getEmail();
        if (email == null || email.isEmpty()) {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getUncommittedEmailAddressMessage());
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
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, src.getName()));
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
                    .execute(new SendEmailTask(plugin, player, transport, message))
                    .submit(plugin);

            //set new password here if the email sending fails fails we have still the old password
            account.setPasswordHash(plugin.getHasher().hash(newPassword));
            plugin.getGame().getScheduler().createTaskBuilder()
                    .async()
                    .execute(new SaveTask(plugin, account))
                    .submit(plugin);
        } catch (UnsupportedEncodingException ex) {
            //we can ignore this, because we will encode with UTF-8 which all Java platforms supports
        } catch (Exception ex) {
            plugin.getLogger().error("Error executing command", ex);
            src.sendMessage(Text.of(TextColors.DARK_RED
                    , plugin.getConfigManager().getConfig().getTextConfig().getErrorCommandMessage()));
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
