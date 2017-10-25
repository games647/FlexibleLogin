package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.Settings;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;

public abstract class AbstractPreventListener {

    protected final FlexibleLogin plugin;
    protected final Settings settings;

    public AbstractPreventListener(FlexibleLogin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    protected void checkLoginStatus(Cancellable event, Player player) {
        if (settings.getGeneral().isBypassPermission() && player.hasPermission(PomData.ARTIFACT_ID + ".bypass")) {
            return;
        }

        if (settings.getGeneral().isCommandOnlyProtection()) {
            //check if the user is already registered
            if (!plugin.getDatabase().getAccount(player).isPresent()
                    && player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired")) {
                event.setCancelled(true);
            }
        } else if (!plugin.getDatabase().isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }
}
