package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class UUIDResetPwTask extends ResetPwTask {

    private final UUID uuid;

    public UUIDResetPwTask(CommandSource src, String password, UUID uuid) {
        super(src, password);

        this.uuid = uuid;
    }

    @Override
    public Optional<Player> getIfPresent() {
        return Sponge.getServer().getPlayer(uuid);
    }

    @Override
    public Optional<Account> loadAccount() {
        return Optional.ofNullable(plugin.getDatabase().loadAccount(uuid));
    }
}
