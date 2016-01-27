package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Config;

import java.util.Arrays;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class PlayerConnectionListener {

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect playerQuitEvent) {
        Player player = playerQuitEvent.getTargetEntity();
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account != null) {
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);
        }
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join playerJoinEvent) {
        Player player = playerJoinEvent.getTargetEntity();
        if (!player.getName().matches(VALID_USERNAME)) {
            //validate invalid characters
            player.kick(plugin.getConfigManager().getConfig().getTextConfig().getInvalidUsername());
            playerJoinEvent.setMessage(Text.EMPTY);
        }

        plugin.getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(() -> {
                    Account loadedAccount = plugin.getDatabase().loadAccount(player);
                    byte[] newIp = player.getConnection().getAddress().getAddress().getAddress();

                    Config config = plugin.getConfigManager().getConfig();
                    if (loadedAccount == null) {
                        if (config.isCommandOnlyProtection()) {
                            if (player.hasPermission(plugin.getContainer().getId() + ".registerRequired")) {
                                //command only protection but have to register
                                player.sendMessage(config.getTextConfig().getNotLoggedInMessage());
                            }
                        } else {
                            //no account
                            player.sendMessage(config.getTextConfig().getNotLoggedInMessage());
                        }
                    } else if (config.isIpAutoLogin() && Arrays.equals(loadedAccount.getIp(), newIp)) {
                        //user will be auto logged in
                        player.sendMessage(config.getTextConfig().getIpAutoLogin());
                        loadedAccount.setLoggedIn(true);
                    } else {
                        //user has an account but isn't logged in
                        player.sendMessage(config.getTextConfig().getNotLoggedInMessage());
                    }
                })
                .submit(plugin);
    }
}
