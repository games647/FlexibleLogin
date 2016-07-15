/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
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

package com.github.games647.flexiblelogin.listener;

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.List;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class PreventListener {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Listener
    public void onPlayerMove(MoveEntityEvent playerMoveEvent, @First Player player) {
        Vector3d oldLocation = playerMoveEvent.getFromTransform().getPosition();
        Vector3d newLocation = playerMoveEvent.getToTransform().getPosition();
        if ((oldLocation.getFloorX() != newLocation.getFloorX()
                || oldLocation.getFloorZ() != newLocation.getFloorZ())) {
            checkLoginStatus(playerMoveEvent, player);
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat chatEvent, @First Player player) {
        checkLoginStatus(chatEvent, player);
    }

    @Listener
    public void onCommand(SendCommandEvent commandEvent, @First Player player) {
        String command = commandEvent.getCommand();
        //do not blacklist our own commands
        if ("register".equals(command) || "login".equals(command)
                || "forgotpassword".equals(command)) {
            return;
        }

        if (plugin.getConfigManager().getConfig().isCommandOnlyProtection()) {
            List<String> protectedCommands = plugin.getConfigManager().getConfig().getProtectedCommands();
            if ((protectedCommands.isEmpty() || protectedCommands.contains(command))) {
                if (!plugin.getDatabase().isLoggedin(player)) {
                    player.sendMessage(plugin.getConfigManager().getTextConfig().getProtectedCommand());
                    commandEvent.setCancelled(true);
                }
            }
        } else {
            checkLoginStatus(commandEvent, player);
        }
    }

    @Listener
    public void onPlayerItemDrop(DropItemEvent.Dispense dropItemEvent, @First Player player) {
        checkLoginStatus(dropItemEvent, player);
    }

    @Listener
    public void onItemConsume(UseItemStackEvent.Start itemConsumeEvent, @First Player player) {
        checkLoginStatus(itemConsumeEvent, player);
    }

    @Listener
    public void onInventoryChange(ChangeInventoryEvent breakBlockEvent, @First Player player) {
        checkLoginStatus(breakBlockEvent, player);
    }

    @Listener
    public void onBlockInteract(InteractBlockEvent interactBlockEvent, @First Player player) {
        checkLoginStatus(interactBlockEvent, player);
    }

    @Listener
    public void onPlayerDamage(DamageEntityEvent damageEntityEvent, @First Player player) {
        checkLoginStatus(damageEntityEvent, player);
    }

    @Listener
    public void onDamagePlayer(DamageEntityEvent damageEntityEvent) {
        //check the target
        Entity targetEntity = damageEntityEvent.getTargetEntity();
        //check only if the event isn't already cancelled by the first call
        if (targetEntity instanceof Player) {
            checkLoginStatus(damageEntityEvent, (Player) damageEntityEvent.getTargetEntity());
        }
    }

    private void checkLoginStatus(Cancellable event, Player player) {
        if (plugin.getConfigManager().getConfig().isBypassPermission()
                && player.hasPermission(plugin.getContainer().getId() + ".bypass")) {
            return;
        }

        if (plugin.getConfigManager().getConfig().isCommandOnlyProtection()) {
            //check if the user is already registered
            if (plugin.getDatabase().getAccountIfPresent(player) == null
                && player.hasPermission(plugin.getContainer().getId()+ ".registerRequired")) {
                event.setCancelled(true);
            }
        } else if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
