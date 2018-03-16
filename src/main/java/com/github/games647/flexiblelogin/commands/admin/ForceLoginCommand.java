/*
 * The MIT License
 *
 * Copyright 2018 Toranktto, contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.games647.flexiblelogin.commands.admin;

import com.github.games647.flexiblelogin.AttemptManager;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.commands.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.ForceLoginTask;
import com.github.games647.flexiblelogin.validation.NamePredicate;
import com.google.inject.Inject;
import java.util.Optional;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import static org.spongepowered.api.text.Text.of;

public class ForceLoginCommand extends AbstractCommand {

    @Inject
    private NamePredicate namePredicate;

    private final AttemptManager attemptManager;
    private final CommandManager commandManager;

    @Inject
    ForceLoginCommand(FlexibleLogin plugin, Logger logger, Settings settings, AttemptManager attemptManager,
            CommandManager commandManager) {
        super(plugin, logger, settings);
        this.attemptManager = attemptManager;
        this.commandManager = commandManager;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        String accountId = args.<String>getOne("account").get();

        if (!namePredicate.test(accountId)) {
            src.sendMessage(settings.getText().getInvalidUsername());
            return CommandResult.success();
        }

        Optional<Player> optPlayer = Sponge.getServer().getPlayer(accountId);

        if (!optPlayer.isPresent()) {
            src.sendMessage(settings.getText().getForceLoginOffline());
            return CommandResult.success();
        }

        Player player = optPlayer.get();

        if (plugin.getDatabase().isLoggedIn(player)) {
            src.sendMessage(settings.getText().getForceLoginAlreadyLoggedIn());
            return CommandResult.success();
        }

        Task.builder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new ForceLoginTask(plugin, attemptManager, src, player))
                .name("Force Login Query")
                .submit(plugin);

        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .arguments(onlyOne(string(of("account"))))
                .build();
    }
}
