package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;

public class UnregisterTask implements Runnable {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();
    private final CommandSource src;

    private final Object accountIndentifer;

    public UnregisterTask(CommandSource src, UUID uuid) {
        this.src = src;

        this.accountIndentifer = uuid;
    }

    public UnregisterTask(CommandSource src, String playerName) {
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
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig()
                    .getAccountDeleted(accountIndentifer.toString()));
        } else {
            src.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getAccountNotFound());
        }
    }
}
