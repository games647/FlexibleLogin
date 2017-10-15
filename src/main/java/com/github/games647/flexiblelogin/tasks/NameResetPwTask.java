package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class NameResetPwTask extends ResetPwTask {

    private final String username;

    public NameResetPwTask(FlexibleLogin plugin, CommandSource src, String password, String username) {
        super(plugin, src, password);
        this.username = username;
    }

    @Override
    public Optional<Player> getIfPresent() {
        return Sponge.getServer().getPlayer(username);
    }

    @Override
    public Optional<Account> loadAccount() {
        return plugin.getDatabase().loadAccount(username);
    }
}
