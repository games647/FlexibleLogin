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
            checkAllowance(playerMoveEvent, playerMoveEvent.getTargetEntity());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onChat(MessageSinkEvent chatEvent) {
        Optional<Player> playerOptional = chatEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(chatEvent, playerOptional.get());
        }
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

            checkAllowance(commandEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemDrop(DropItemStackEvent.Drop dropItemEvent) {
        Optional<Player> playerOptional = dropItemEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(dropItemEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemPickup(PickUpItemEvent pickUpItemEvent) {
        Optional<Player> playerOptional = pickUpItemEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(pickUpItemEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onItemConsume(UseItemStackEvent itemConsumeEvent) {
        Optional<Player> playerOptional = itemConsumeEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(itemConsumeEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onBlockBreak(BreakBlockEvent breakBlockEvent) {
        Optional<Player> playerOptional = breakBlockEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(breakBlockEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onBlockBreak(PlaceBlockEvent blockPlaceEvent) {
        Optional<Player> playerOptional = blockPlaceEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(blockPlaceEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onBlockChange(ChangeBlockEvent changeBlockEvent) {
        Optional<Player> playerOptional = changeBlockEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(changeBlockEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onBlockInteract(InteractBlockEvent interactBlockEvent) {
        Optional<Player> playerOptional = interactBlockEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(interactBlockEvent, playerOptional.get());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onEntityInteract(InteractEntityEvent interactEntityEvent) {
        Optional<Player> playerOptional = interactEntityEvent.getCause().first(Player.class);
        if (playerOptional.isPresent()) {
            checkAllowance(interactEntityEvent, playerOptional.get());
        }
    }

    private void checkAllowance(Cancellable event, Player player) {
        if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
