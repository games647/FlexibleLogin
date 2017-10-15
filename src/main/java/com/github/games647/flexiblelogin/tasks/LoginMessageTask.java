package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class LoginMessageTask implements Consumer<Task> {

    private final FlexibleLogin plugin;
    private final Player player;

    public LoginMessageTask(FlexibleLogin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void accept(Task task) {
        Optional<Account> optAccount = plugin.getDatabase().getAccount(player);
        if (optAccount.isPresent()) {
            if (optAccount.get().isLoggedIn()) {
                task.cancel();
            } else {
                player.sendMessage(plugin.getConfigManager().getText().getNotLoggedIn());
            }
        } else {
            player.sendMessage(plugin.getConfigManager().getText().getNotRegistered());
        }
    }
}
