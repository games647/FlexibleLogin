package com.github.games647.flexiblelogin.listener;

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;

public class PreventListener {

    private final FlexibleLogin plugin = FlexibleLogin.getInstance();

    @Listener
    public void onPlayerMove(DisplaceEntityEvent.TargetPlayer playerMoveEvent) {
        Vector3d oldLocation = playerMoveEvent.getFromTransform().getPosition();
        Vector3d newLocation = playerMoveEvent.getToTransform().getPosition();
        if ((oldLocation.getFloorX()!= newLocation.getFloorX()
                || oldLocation.getFloorZ()!= newLocation.getFloorZ())) {
            checkLoginStatus(playerMoveEvent, playerMoveEvent.getTargetEntity());
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat chatEvent) {
        checkLoginStatus(chatEvent, chatEvent);
    }

    @Listener
    public void onCommand(SendCommandEvent commandEvent) {
        Optional<Player> playerOptional = commandEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            String command = commandEvent.getCommand();
            //do not blacklist our own commands
            if ("register".equals(command) || "login".equals(command)
                    || "forgotpassword".equals(command)) {
                return;
            }

            if (plugin.getConfigManager().getConfig().isCommandOnlyProtection()) {
                Player player = playerOptional.get();

                List<String> protectedCommands = plugin.getConfigManager().getConfig().getProtectedCommands();
                if ((protectedCommands.isEmpty() || protectedCommands.contains(command))) {
                    if (!plugin.getDatabase().isLoggedin(player)) {
                        player.sendMessage(plugin.getConfigManager().getConfig().getTextConfig().getProtectedCommand());
                        commandEvent.setCancelled(true);
                    }
                }
            } else {
                checkLoginStatus(commandEvent, playerOptional.get());
            }
        }
    }

    @Listener
    public void onPlayerItemDrop(DropItemEvent dropItemEvent) {
        checkLoginStatus(dropItemEvent, dropItemEvent);
    }

    @Listener
    public void onItemConsume(UseItemStackEvent itemConsumeEvent) {
        checkLoginStatus(itemConsumeEvent, itemConsumeEvent);
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break breakBlockEvent) {
        checkLoginStatus(breakBlockEvent, breakBlockEvent);
    }

    @Listener
    public void onInventoryChange(ChangeInventoryEvent breakBlockEvent) {
        checkLoginStatus(breakBlockEvent, breakBlockEvent);
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place blockPlaceEvent) {
        checkLoginStatus(blockPlaceEvent, blockPlaceEvent);
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent changeBlockEvent) {
        checkLoginStatus(changeBlockEvent, changeBlockEvent);
    }

    @Listener
    public void onBlockInteract(InteractBlockEvent interactBlockEvent) {
        checkLoginStatus(interactBlockEvent, interactBlockEvent);
    }

    @Listener
    public void onEntityInteract(InteractEntityEvent interactEntityEvent) {
        checkLoginStatus(interactEntityEvent, interactEntityEvent);
    }

    private void checkLoginStatus(Cancellable event, Event causeEvent) {
        Optional<Player> playerOptional = causeEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkLoginStatus(event, playerOptional.get());
        }
    }

    private void checkLoginStatus(Cancellable event, Player player) {
        if (plugin.getConfigManager().getConfig().isCommandOnlyProtection()) {
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
