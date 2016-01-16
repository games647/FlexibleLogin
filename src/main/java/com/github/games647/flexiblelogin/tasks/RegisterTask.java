package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.hasher.TOTP;

import org.spongepowered.api.entity.living.player.Player;

import java.net.MalformedURLException;
import java.net.URL;

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
                //thread-safe, because it's immutable after config load
                if (plugin.getConfigManager().getConfig().getHashAlgo().equalsIgnoreCase("totp")) {
                    sendTotpHint(hashedPassword);
                }

                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountCreatedMessage());
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getUnexpectedErrorMessage());
            }
        } else {
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountExistsMessage());
        }
    }

    private void sendTotpHint(String secretCode) {
        //I assume this thread-safe, because PlayerChat is also in an async task
        String host = plugin.getGame().getServer().getBoundAddress().get().getAddress().getCanonicalHostName();
        try {
            URL barcodeUrl = new URL(TOTP.getQRBarcodeURL(player.getName(), host, secretCode));
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getSecretKeyCreatedMessageHeader());
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getSecretKeyCreatedMessage(secretCode, barcodeUrl.toString()));
        }catch (MalformedURLException ex) {
            plugin.getLogger().error("Malformed totp url link", ex);
        }
    }
}
