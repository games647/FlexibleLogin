package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

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
            src.sendMessage(Texts.of(TextColors.DARK_GREEN, "Deleted account of: " + accountIndentifer));
        } else {
            src.sendMessage(Texts.of(TextColors.DARK_RED, "User account not found"));
        }
    }
}
