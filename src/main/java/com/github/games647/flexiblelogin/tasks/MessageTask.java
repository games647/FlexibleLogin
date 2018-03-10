package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.Settings;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

public class MessageTask implements Runnable {

    private final FlexibleLogin plugin;
    private final Settings settings;

    public MessageTask(FlexibleLogin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void run() {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(this::isNotBypassed)
                .filter(this::isRegistrationRequired)
                .forEach(this::sendMessage);
    }

    private void sendMessage(Player player) {
        Optional<Account> optAccount = plugin.getDatabase().getAccount(player);
        if (optAccount.isPresent()) {
            Account account = optAccount.get();
            if (!account.isLoggedIn()) {
                player.sendMessage(settings.getText().getNotLoggedIn());
            }
        } else {
            player.sendMessage(settings.getText().getNotRegistered());
        }
    }

    private boolean isNotBypassed(Subject player) {
        //send the message if the player only needs to login
        return settings.getGeneral().isBypassPermission() && player.hasPermission(PomData.ARTIFACT_ID + ".bypass");
    }

    private boolean isRegistrationRequired(Subject player) {
        return !settings.getGeneral().isCommandOnlyProtection()
                || player.hasPermission(PomData.ARTIFACT_ID + ".registerRequired");
    }
}
