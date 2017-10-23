package com.github.games647.flexiblelogin.commands;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.config.Settings;

import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.permission.Subject;

import static com.github.games647.flexiblelogin.PomData.ARTIFACT_ID;

public abstract class AbstractCommand implements CommandExecutor {

    protected final FlexibleLogin plugin;
    protected final Settings settings;

    private final String permission;
    private final Pattern uuidPattern = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    public AbstractCommand(FlexibleLogin plugin, Settings settings, String permissionKey) {
        this.plugin = plugin;
        this.settings = settings;
        this.permission = ARTIFACT_ID + ".command." + permissionKey;
    }

    public AbstractCommand(FlexibleLogin plugin, Settings settings) {
        this(plugin, settings, "");
    }

    public void checkPlayerPermission(Subject player) throws CommandPermissionException {
        if (settings.getGeneral().isPlayerPermissions() && !player.hasPermission(permission)) {
            throw new CommandPermissionException();
        }
    }

    public boolean isValidUUID(String input) {
        return uuidPattern.matcher(input).matches();
    }
}
