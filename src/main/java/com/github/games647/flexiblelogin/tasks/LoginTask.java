package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;

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
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountDoesNotExistMessage());
            return;
        }

        try {
            if (account.checkPassword(plugin, userInput)) {
                account.setLoggedIn(true);
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getLoggedInMessage());
            } else {
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getIncorrectPasswordMessage());
            }
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error while password checking", ex);
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getUnexpectedErrorMessage());
        }
    }
}
