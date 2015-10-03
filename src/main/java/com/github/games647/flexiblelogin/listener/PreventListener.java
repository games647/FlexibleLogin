package com.github.games647.flexiblelogin.listener;

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.google.common.base.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.BreakBlockEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.PlaceBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.inventory.DropItemStackEvent;
import org.spongepowered.api.event.inventory.PickUpItemEvent;
import org.spongepowered.api.event.inventory.UseItemStackEvent;

public class PreventListener {

    private final FlexibleLogin plugin;

    public PreventListener(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerMove(DisplaceEntityEvent.TargetPlayer playerMoveEvent) {
        Vector3d oldLocation = playerMoveEvent.getFromTransform().getPosition();
        Vector3d newLocation = playerMoveEvent.getToTransform().getPosition();
        if ((oldLocation.getFloorX()!= newLocation.getFloorX()
                || oldLocation.getFloorZ()!= newLocation.getFloorZ())) {
            checkLoginStatus(playerMoveEvent, playerMoveEvent.getTargetEntity());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onChat(MessageSinkEvent.Chat chatEvent) {
        checkLoginStatus(chatEvent, chatEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onCommand(SendCommandEvent commandEvent) {
        Optional<Player> playerOptional = commandEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            String command = commandEvent.getCommand();
            //do not blacklist our own commands
            if ("register".equals(command) || "login".equals(command)
                    || "forgotpassword".equals(command)) {
                return;
            }

            checkLoginStatus(commandEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemDrop(DropItemStackEvent.Drop dropItemEvent) {
        Optional<Player> playerOptional = dropItemEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkLoginStatus(dropItemEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemPickup(PickUpItemEvent pickUpItemEvent) {
        checkLoginStatus(pickUpItemEvent, pickUpItemEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onItemConsume(UseItemStackEvent itemConsumeEvent) {
        checkLoginStatus(itemConsumeEvent, itemConsumeEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockBreak(BreakBlockEvent breakBlockEvent) {
        checkLoginStatus(breakBlockEvent, breakBlockEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockBreak(PlaceBlockEvent blockPlaceEvent) {
        checkLoginStatus(blockPlaceEvent, blockPlaceEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockChange(ChangeBlockEvent changeBlockEvent) {
        checkLoginStatus(changeBlockEvent, changeBlockEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockInteract(InteractBlockEvent interactBlockEvent) {
        checkLoginStatus(interactBlockEvent, interactBlockEvent.getCause());
    }

    @Listener(ignoreCancelled = true)
    public void onEntityInteract(InteractEntityEvent interactEntityEvent) {
        checkLoginStatus(interactEntityEvent, interactEntityEvent.getCause());
    }

    private void checkLoginStatus(Cancellable event, Cause cause) {
        Optional<Player> playerOptional = cause.first(Player.class);
        if (playerOptional.isPresent()) {
            checkLoginStatus(event, playerOptional.get());
        }
    }

    private void checkLoginStatus(Cancellable event, Player player) {
        if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
