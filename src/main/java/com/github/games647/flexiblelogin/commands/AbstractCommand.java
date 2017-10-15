package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;

import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.permission.Subject;

public abstract class AbstractCommand implements CommandExecutor {

    protected final FlexibleLogin plugin;
    private final String permissionKey;

    private final Pattern uuidPattern = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    public AbstractCommand(FlexibleLogin plugin, String permissionKey) {
        this.plugin = plugin;
        this.permissionKey = permissionKey;
    }

    public AbstractCommand(FlexibleLogin plugin) {
        this(plugin, "");
    }

    public void checkPlayerPermission(Subject player) throws CommandPermissionException {
        if (plugin.getConfigManager().getGeneral().isPlayerPermissions()
                && !player.hasPermission(PomData.ARTIFACT_ID + ".command." + permissionKey)) {
            throw new CommandPermissionException();
        }
    }

    public boolean isValidUUID(String input) {
        return uuidPattern.matcher(input).matches();
    }
}
