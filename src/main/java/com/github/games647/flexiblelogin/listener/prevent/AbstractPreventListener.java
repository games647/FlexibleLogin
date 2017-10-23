package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;

public abstract class AbstractPreventListener {

    protected final FlexibleLogin plugin;

    public AbstractPreventListener(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    protected void checkLoginStatus(Cancellable event, Player player) {
        if (plugin.getConfigManager().getGeneral().isBypassPermission()
                && player.hasPermission(PomData.ARTIFACT_ID + ".bypass")) {
            return;
        }

        if (plugin.getConfigManager().getGeneral().isCommandOnlyProtection()) {
            //check if the user is already registered
            if (!plugin.getDatabase().getAccount(player).isPresent()
                    && player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired")) {
                event.setCancelled(true);
            }
        } else if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
