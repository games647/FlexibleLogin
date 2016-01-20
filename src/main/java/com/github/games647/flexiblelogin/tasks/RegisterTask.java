package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.hasher.TOTP;

import java.net.MalformedURLException;
import java.net.URL;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class RegisterTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private final Player player;
    private final String password;

    public RegisterTask(Player player, String password) {
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

                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountCreated());
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getErrorCommandMessage());
            }
        } else {
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountAlreadyExists());
        }
    }

    private void sendTotpHint(String secretCode) {
        //I assume this thread-safe, because PlayerChat is also in an async task
        String host = plugin.getGame().getServer().getBoundAddress().get().getAddress().getCanonicalHostName();
        try {
            URL barcodeUrl = new URL(TOTP.getQRBarcodeURL(player.getName(), host, secretCode));
            player.sendMessage(Text.builder()
                    .append(plugin.getConfigManager().getConfig().getTextConfig().getKeyGenerated())
                    .build());
            player.sendMessage(Text.builder(secretCode)
                    .color(TextColors.GOLD)
                    .append(Text.of(TextColors.DARK_BLUE, " / "))
                    .append(Text.builder()
                            .append(plugin.getConfigManager().getConfig().getTextConfig().getScanQr())
                            .onClick(TextActions.openUrl(barcodeUrl))
                            .build())
                    .build());
        } catch (MalformedURLException ex) {
            plugin.getLogger().error("Malformed totp url link", ex);
        }
    }
}
