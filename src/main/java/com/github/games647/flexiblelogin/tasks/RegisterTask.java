package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.hasher.TOTP;

import java.net.MalformedURLException;
import java.net.URL;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class RegisterTask implements Runnable {

    private final FlexibleLogin plugin;

    private final Player player;
    private final String password;

    public RegisterTask(FlexibleLogin plugin, Player player, String password) {
        this.plugin = plugin;

        this.player = player;
        this.password = password;
    }

    @Override
    public void run() {
        if (plugin.getDatabase().loadAccount(player) == null) {
            try {
                String hashedPassword = plugin.getHasher().hash(password);
                plugin.getDatabase().createAccount(player, hashedPassword);
                if (plugin.getConfigManager().getConfiguration().getHashAlgo().equalsIgnoreCase("totp")) {
                    sendTotpHint(hashedPassword);
                }

                player.sendMessage(Texts.of(TextColors.DARK_GREEN, "Account created"));
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
            }
        } else {
            player.sendMessage(Texts.of(TextColors.DARK_RED, "Your account already exists"));
        }
    }

    private void sendTotpHint(String secretCode) {
        String host = plugin.getGame().getServer().getBoundAddress().get().getAddress().getCanonicalHostName();
        try {
            URL barcodeUrl = new URL(TOTP.getQRBarcodeURL(player.getName(), host, secretCode));
            player.sendMessage(Texts.builder("SecretKey genereted: ")
                    .color(TextColors.DARK_GREEN)
                    .build());
            player.sendMessage(Texts.builder(secretCode)
                    .color(TextColors.GOLD)
                    .append(Texts.of(TextColors.DARK_BLUE, " or "))
                    .append(Texts.builder("Click here to scan the QR-Code")
                            .color(TextColors.GOLD)
                            .onClick(TextActions.openUrl(barcodeUrl))
                            .build())
                    .build());
        }catch (MalformedURLException ex) {
            plugin.getLogger().error("Malformed totp url link", ex);
        }
    }
}
