/*
 * This file is part of FlexibleLogin, licensed under the MIT License (MIT).
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
