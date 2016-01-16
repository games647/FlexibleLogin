package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.command.CommandSource;

import java.util.UUID;

public class UnregisterTask implements Runnable {

    private final FlexibleLogin plugin;
    private final CommandSource src;

    private final Object accountIndentifer;

    public UnregisterTask(FlexibleLogin plugin, CommandSource src, UUID uuid) {
        this.plugin = plugin;
        this.src = src;

        this.accountIndentifer = uuid;
    }

    public UnregisterTask(FlexibleLogin plugin, CommandSource src, String playerName) {
        this.plugin = plugin;
        this.src = src;

        this.accountIndentifer = playerName;
    }

    @Override
    public void run() {
        boolean accountFound;
        if (accountIndentifer instanceof String) {
            accountFound = plugin.getDatabase().deleteAccount((String) accountIndentifer);
        } else {
            accountFound = plugin.getDatabase().deleteAccount((UUID) accountIndentifer);
        }

        if (accountFound) {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountDeletedMessage((String) accountIndentifer));
        } else {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountNotFoundMessage());
        }
    }
}
