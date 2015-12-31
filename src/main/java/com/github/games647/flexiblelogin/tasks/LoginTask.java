package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class LoginTask implements Runnable {

    private final FlexibleLogin plugin;

    private final Player player;
    private final String userInput;

    public LoginTask(FlexibleLogin plugin, Player player, String password) {
        this.plugin = plugin;

        this.player = player;
        this.userInput = password;
    }

    @Override
    public void run() {
        Account account = plugin.getDatabase().loadAccount(player);
        if (account == null) {
            player.sendMessage(Text.of(TextColors.DARK_RED, "Your account doesn't exist"));
            return;
        }

        try {
            if (account.checkPassword(plugin, userInput)) {
                account.setLoggedIn(true);
                player.sendMessage(Text.of(TextColors.DARK_GREEN, "Logged in"));
            } else {
                player.sendMessage(Text.of(TextColors.DARK_RED, "Incorrect password"));
            }
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error while password checking", ex);
            player.sendMessage(Text.of(TextColors.DARK_RED, "Error executing command. See console"));
        }
    }
}
