package com.github.games647.flexiblelogin.listener.prevent;

import com.github.games647.flexiblelogin.PomData;
import com.google.inject.Inject;

import java.util.Optional;

import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

/**
 * Certain plugins log commands. As those commands could contain passwords, our plugin should be ignored. Currently
 * the plugins do not provide an API to do that, so we will cancel our command event and enable it afterwards before it
 * gets executed.
 */
public class ChatLoggerListener {

    @Inject
    private CommandManager commandManager;

    @Listener
    public void onCommand(SendCommandEvent commandEvent) {
        String command = commandEvent.getCommand();

        Optional<? extends CommandMapping> commandOpt = commandManager.get(command);
        if (isOurCommand(command)) {
            //fake the cancelled event
            commandEvent.setCancelled(true);
        }
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.TRUE)
    public void onPostCommand(SendCommandEvent commandEvent) {
        String command = commandEvent.getCommand();
        if (isOurCommand(command)) {
            //re-enable it
            commandEvent.getContext();
            commandEvent.setCancelled(false);
        }
    }

    private boolean isOurCommand(String command) {
        Optional<? extends CommandMapping> commandOpt = commandManager.get(command);
        return commandOpt.map(commandMapping -> commandManager.getOwner(commandMapping)
                .map(pc -> pc.getId().equals(PomData.ARTIFACT_ID))
                .orElse(false));
    }
}
