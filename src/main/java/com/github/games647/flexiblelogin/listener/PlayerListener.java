package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import org.spongepowered.api.entity.living.player.Player;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.living.player.PlayerQuitEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

public class PlayerListener {

    private static final String VALID_USERNAME = "^\\w{2,16}$";

    private final FlexibleLogin plugin;

    public PlayerListener(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getSource();
        if (!player.getName().matches(VALID_USERNAME)) {
            //validate invalid characters
            player.kick(Texts.of(TextColors.DARK_RED
                    , "Invalid username - Choose characters a-z,A-Z,0-9 or _ and a length between 2 and 16"));
            playerJoinEvent.setNewMessage(Texts.of());
        }

        player.sendMessage(Texts.of(TextColors.DARK_AQUA, "Type /register or /login to login in"));
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getSource();
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account != null) {
            //account is loaded -> mark the player as logout as it could remain in the cache
            account.setLoggedIn(false);
        }
    }
}
