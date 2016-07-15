package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.function.Consumer;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class LoginMessageTask implements Consumer<Task> {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();
    private final Player player;

    public LoginMessageTask(Player player) {
        this.player = player;
    }

    @Override
    public void accept(Task task) {
        Account account = plugin.getDatabase().getAccountIfPresent(player);
        if (account != null && account.isLoggedIn()) {
            task.cancel();
            return;
        }

        if (account == null) {
            player.sendMessage(plugin.getConfigManager().getTextConfig().getNotRegisteredMessage());
        } else {
            player.sendMessage(plugin.getConfigManager().getTextConfig().getNotLoggedInMessage());
        }
    }
}
