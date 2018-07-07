package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.PomData;
import com.google.inject.Inject;

import java.util.Optional;

import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

public class ChatLoggerListener {

    @Inject
    private CommandManager commandManager;

    @Listener(order = Order.POST)
    public void onPostCommand(SendCommandEvent commandEvent, @First CommandSource src) {
        String command = commandEvent.getCommand();

        Optional<? extends CommandMapping> commandOpt = commandManager.get(command);
        if (commandOpt.isPresent()) {
            if (commandManager.getOwner(commandOpt.get())
                    .map(pc -> pc.getId().equals(PomData.ARTIFACT_ID))
                    .orElse(false)) {
                commandEvent.setCancelled(true);
            }
        }
    }
}
