package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class UUIDResetPwTask extends ResetPwTask {

    private final UUID uuid;

    public UUIDResetPwTask(FlexibleLogin plugin, CommandSource src, String password, UUID uuid) {
        super(plugin, src, password);
        this.uuid = uuid;
    }

    @Override
    public Optional<Player> getIfPresent() {
        return Sponge.getServer().getPlayer(uuid);
    }

    @Override
    public Optional<Account> loadAccount() {
        return plugin.getDatabase().loadAccount(uuid);
    }
}
