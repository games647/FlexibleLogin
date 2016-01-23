package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;

public class LoginTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    private final Player player;
    private final String userInput;

    public LoginTask(Player player, String password) {
        this.player = player;
        this.userInput = password;
    }

    @Override
    public void run() {
        Account account = plugin.getDatabase().loadAccount(player);
        if (account == null) {
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountNotFound());
            return;
        }

        try {
            if (account.checkPassword(plugin, userInput)) {
                account.setLoggedIn(true);
                //update the ip
                byte[] playerIp = player.getConnection().getAddress().getAddress().getAddress();
                account.setIp(playerIp);

                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getLoggedIn());
            } else {
                player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getIncorrectPassword());
            }
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error while password checking", ex);
            player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getErrorCommandMessage());
        }
    }
}
