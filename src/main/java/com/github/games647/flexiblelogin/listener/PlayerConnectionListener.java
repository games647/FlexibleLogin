package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Arrays;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class PlayerConnectionListener {

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

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
                    if (plugin.getConfigManager().getConfig().isIpAutoLogin()
                            && Arrays.equals(loadedAccount.getIp(), newIp)) {
                        player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getIpAutoLogin());
                        loadedAccount.setLoggedIn(true);
                    } else if (!plugin.getConfigManager().getConfig().isCommandOnlyProtection()
                            || (loadedAccount == null
                                && player.hasPermission(plugin.getContainer().getId() + ".registerRequired"))) {
                        player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getNotLoggedInMessage());
                    }
                })
                .submit(plugin);
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect playerQuitEvent) {
        Player player = playerQuitEvent.getTargetEntity();
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account != null) {
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);
        }
    }
}
