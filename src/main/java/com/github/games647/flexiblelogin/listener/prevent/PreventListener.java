/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
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
package com.github.games647.flexiblelogin.listener.prevent;

import com.flowpowered.math.vector.Vector3i;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.PomData;
import com.github.games647.flexiblelogin.config.Settings;
import com.google.inject.Inject;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent.NumberPress;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class PreventListener extends AbstractPreventListener {

    private final CommandManager commandManager;

    @Inject PreventListener(FlexibleLogin plugin, Settings settings, CommandManager commandManager) {
        super(plugin, settings);

        this.commandManager = commandManager;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerMove(MoveEntityEvent playerMoveEvent, @First Player player) {
        if (playerMoveEvent instanceof MoveEntityEvent.Teleport) {
            return;
        }

        Vector3i oldLocation = playerMoveEvent.getFromTransform().getPosition().toInt();
        Vector3i newLocation = playerMoveEvent.getToTransform().getPosition().toInt();
        if (oldLocation.getX() != newLocation.getX() || oldLocation.getZ() != newLocation.getZ()) {
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

        Optional<? extends CommandMapping> commandOpt = commandManager.get(command);
        if (commandOpt.isPresent()) {
            CommandMapping mapping = commandOpt.get();
            command = mapping.getPrimaryAlias();

            //do not blacklist our own commands
            if (commandManager.getOwner(mapping)
                .map(pc -> pc.getId().equals(PomData.ARTIFACT_ID))
                .orElse(false)) {
                return;
            }
        }

        commandEvent.setResult(CommandResult.empty());
        if (settings.getGeneral().isCommandOnlyProtection()) {
            List<String> protectedCommands = settings.getGeneral().getProtectedCommands();
            if (protectedCommands.contains(command) && !plugin.getDatabase().isLoggedIn(player)) {
                player.sendMessage(settings.getText().getProtectedCommand());
                commandEvent.setCancelled(true);
            }
        } else {
            checkLoginStatus(commandEvent, player);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemDrop(DropItemEvent dropItemEvent, @First Player player) {
        checkLoginStatus(dropItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerItemPickup(ChangeInventoryEvent.Pickup pickupItemEvent, @Root Player player) {
        checkLoginStatus(pickupItemEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemConsume(UseItemStackEvent.Start itemConsumeEvent, @First Player player) {
        checkLoginStatus(itemConsumeEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemInteract(InteractItemEvent interactItemEvent, @First Player player) {
        checkLoginStatus(interactItemEvent, player);
    }

    // Ignore number press events, because Sponge before this commit
    // https://github.com/SpongePowered/SpongeForge/commit/f0605fb0bd62ca2f958425378776608c41f16cca
    // has a duplicate bug. Using this exclude we can ignore it, but still cancel the movement of the item
    // it appears to be fixed using SpongeForge 4005 (fixed) and 4004 with this change
    @Exclude(NumberPress.class)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryChange(ChangeInventoryEvent changeInventoryEvent, @First Player player) {
        checkLoginStatus(changeInventoryEvent, player);
    }

    @Exclude(NumberPress.class)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryInteract(InteractInventoryEvent interactInventoryEvent, @First Player player) {
        checkLoginStatus(interactInventoryEvent, player);
    }

    @Exclude(NumberPress.class)
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInventoryClick(ClickInventoryEvent clickInventoryEvent, @First Player player) {
        checkLoginStatus(clickInventoryEvent, player);
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
        //player is damage source
        checkLoginStatus(damageEntityEvent, player);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onDamagePlayer(DamageEntityEvent damageEntityEvent, @Getter("getTargetEntity") Player player) {
        //player is damage target
        checkLoginStatus(damageEntityEvent, player);
    }
}
