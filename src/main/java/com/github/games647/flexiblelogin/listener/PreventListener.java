package com.github.games647.flexiblelogin.listener;

import com.github.games647.flexiblelogin.FlexibleLogin;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerChangeBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerDropItemEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractEntityEvent;
import org.spongepowered.api.event.entity.player.PlayerItemConsumeEvent;
import org.spongepowered.api.event.entity.player.PlayerMoveEvent;
import org.spongepowered.api.event.entity.player.PlayerPickUpItemEvent;
import org.spongepowered.api.event.message.CommandEvent;
import org.spongepowered.api.world.Location;

public class PreventListener {

    private final FlexibleLogin plugin;

    public PreventListener(FlexibleLogin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Location oldLocation = playerMoveEvent.getOldLocation();
        Location newLocation = playerMoveEvent.getNewLocation();
        if ((oldLocation.getBlockX() != newLocation.getBlockX()
                || oldLocation.getBlockZ() != newLocation.getBlockZ())) {
            checkAllowance(playerMoveEvent, playerMoveEvent.getEntity());
        }
    }

    @Subscribe(ignoreCancelled = true)
    public void onChat(PlayerChatEvent chatEvent) {
        checkAllowance(chatEvent, chatEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onCommand(CommandEvent commandEvent) {
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

    @Subscribe(ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent dropItemEvent) {
        checkAllowance(dropItemEvent, dropItemEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onPlayerItemPickup(PlayerPickUpItemEvent pickUpItemEvent) {
        checkAllowance(pickUpItemEvent, pickUpItemEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent itemConsumeEvent) {
        checkAllowance(itemConsumeEvent, itemConsumeEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onBlockBreak(PlayerBreakBlockEvent breakBlockEvent) {
        checkAllowance(breakBlockEvent, breakBlockEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onBlockChange(PlayerChangeBlockEvent changeBlockEvent) {
        checkAllowance(changeBlockEvent, changeBlockEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractBlockEvent interactBlockEvent) {
        checkAllowance(interactBlockEvent, interactBlockEvent.getEntity());
    }

    @Subscribe(ignoreCancelled = true)
    public void onBlockChange(PlayerInteractEntityEvent interactEntityEvent) {
        checkAllowance(interactEntityEvent, interactEntityEvent.getEntity());
    }

    private void checkAllowance(Cancellable event, Player player) {
        if (!plugin.getDatabase().isLoggedin(player)) {
            event.setCancelled(true);
        }
    }
}
