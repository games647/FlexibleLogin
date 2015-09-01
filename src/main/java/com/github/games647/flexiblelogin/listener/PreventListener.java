package com.github.games647.flexiblelogin.listener;

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.BreakBlockEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChatEvent;
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
        Vector3d oldLocation = playerMoveEvent.getOldTransform().getPosition();
        Vector3d newLocation = playerMoveEvent.getNewTransform().getPosition();
        if ((oldLocation.getFloorX()!= newLocation.getFloorX()
                || oldLocation.getFloorZ()!= newLocation.getFloorZ())) {
            checkAllowance(playerMoveEvent, playerMoveEvent.getTargetEntity());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onChat(PlayerChatEvent chatEvent) {
        checkAllowance(chatEvent, chatEvent.getSource());
    }

    @Listener(ignoreCancelled = true)
    public void onCommand(SendCommandEvent commandEvent) {
        if (commandEvent.getSource() instanceof Player) {
            String command = commandEvent.getCommand();
            //do not blacklist our own commands
            if ("register".equals(command) || "login".equals(command)
                    || "forgotpassword".equals(command)) {
                return;
            }

            checkAllowance(commandEvent, (Player) commandEvent.getSource());
        }
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemDrop(DropItemStackEvent.SourcePlayer dropItemEvent) {
        checkAllowance(dropItemEvent, dropItemEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onPlayerItemPickup(PickUpItemEvent.SourcePlayer pickUpItemEvent) {
        checkAllowance(pickUpItemEvent, pickUpItemEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onItemConsume(UseItemStackEvent.SourcePlayer itemConsumeEvent) {
        checkAllowance(itemConsumeEvent, itemConsumeEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockBreak(BreakBlockEvent.SourcePlayer breakBlockEvent) {
        checkAllowance(breakBlockEvent, breakBlockEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockChange(ChangeBlockEvent.SourcePlayer changeBlockEvent) {
        checkAllowance(changeBlockEvent, changeBlockEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockInteract(InteractBlockEvent.SourcePlayer interactBlockEvent) {
        checkAllowance(interactBlockEvent, interactBlockEvent.getSourceEntity());
    }

    @Listener(ignoreCancelled = true)
    public void onBlockChange(InteractEntityEvent.SourcePlayer interactEntityEvent) {
        checkAllowance(interactEntityEvent, interactEntityEvent.getSourceEntity());
    }

    private void checkAllowance(Cancellable event, Player player) {
        if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
