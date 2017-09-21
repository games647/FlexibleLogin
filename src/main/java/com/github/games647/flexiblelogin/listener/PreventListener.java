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
import java.util.Optional;
import java.util.stream.Collectors;

import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;

import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;

//import org.spongepowered.api.event.entity.DisplaceEntityEvent;

public class PreventListener {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerMove(MoveEntityEvent playerMoveEvent, @First Player player) {
        Vector3d oldLocation = playerMoveEvent.getFromTransform().getPosition();
        Vector3d newLocation = playerMoveEvent.getToTransform().getPosition();
        if ((oldLocation.getFloorX() != newLocation.getFloorX()
                || oldLocation.getFloorZ() != newLocation.getFloorZ())) {
            checkLoginStatus(playerMoveEvent, player);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onChat(MessageChannelEvent.Chat chatEvent, @First Player player) {
        checkLoginStatus(chatEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onCommand(SendCommandEvent commandEvent, @First Player player) {
        String command = commandEvent.getCommand();

        Optional<? extends CommandMapping> commandOpt = plugin.getGame().getCommandManager().get(command);
        if (commandOpt.isPresent()) {
            command = commandOpt.get().getPrimaryAlias();
        }

        //do not blacklist our own commands
        if (plugin.getGame().getCommandManager()
                .getOwnedBy(plugin.getContainer())
                .stream()
                .map(CommandMapping::getPrimaryAlias)
                .collect(Collectors.toSet())
                .contains(command)) {
            return;
        }

        if (plugin.getConfigManager().getGeneral().isCommandOnlyProtection()) {
            List<String> protectedCommands = plugin.getConfigManager().getGeneral().getProtectedCommands();
            if ((protectedCommands.isEmpty() || protectedCommands.contains(command))) {
                if (!plugin.getDatabase().isLoggedin(player)) {
                    player.sendMessage(plugin.getConfigManager().getText().getProtectedCommand());
                    commandEvent.setCancelled(true);
                }
            }
        } else {
            checkLoginStatus(commandEvent, player);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemDrop(DropItemEvent.Dispense dropItemEvent, @First Player player) {
        checkLoginStatus(dropItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemPickup(ChangeInventoryEvent.Pickup pickupItemEvent, @Root Player player) {
        checkLoginStatus(pickupItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemPickup(CreateClaimEvent createClaimEvent, @Root Player player) {
        checkLoginStatus(createClaimEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemConsume(UseItemStackEvent.Start itemConsumeEvent, @First Player player) {
        checkLoginStatus(itemConsumeEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemInteract(InteractItemEvent interactItemEvent, @First Player player) {
        checkLoginStatus(interactItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryChange(ChangeInventoryEvent changeInventoryEvent, @First Player player) {
        checkLoginStatus(changeInventoryEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryInteract(InteractInventoryEvent interactInventoryEvent, @First Player player) {
        checkLoginStatus(interactInventoryEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryClick(ClickInventoryEvent clickInventoryEvent, @First Player player) {
        checkLoginStatus(clickInventoryEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryDrop(DropItemEvent.Dispense dropItemEvent, @First Player player) {
        checkLoginStatus(dropItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(InteractBlockEvent interactBlockEvent, @First Player player) {
        checkLoginStatus(interactBlockEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractEntity(InteractEntityEvent interactEntityEvent, @First Player player) {
        checkLoginStatus(interactEntityEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerDamage(DamageEntityEvent damageEntityEvent, @First Player player) {
        checkLoginStatus(damageEntityEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDamagePlayer(DamageEntityEvent damageEntityEvent) {
        //check the target
        Entity targetEntity = damageEntityEvent.getTargetEntity();

        //check only if the event isn't already cancelled by the first call
        if (targetEntity instanceof Player) {
            checkLoginStatus(damageEntityEvent, (Player) damageEntityEvent.getTargetEntity());
        }
    }

    private void checkLoginStatus(Cancellable event, Player player) {
        if (plugin.getConfigManager().getGeneral().isBypassPermission()
                && player.hasPermission(plugin.getContainer().getId() + ".bypass")) {
            return;
        }

        if (plugin.getConfigManager().getGeneral().isCommandOnlyProtection()) {
            //check if the user is already registered
            if (plugin.getDatabase().getAccountIfPresent(player) == null
                    && player.hasPermission(plugin.getContainer().getId() + ".registerRequired")) {
                event.setCancelled(true);
            }
        } else if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
