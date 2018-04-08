package com.github.games647.flexiblelogin.command.arg;

import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.validation.MailPredicate;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

public class MailElement extends CommandElement {

    private final MailPredicate mailPredicate;
    private final Settings settings;

    public MailElement(@Nullable Text key, MailPredicate mailPredicate, Settings settings) {
        super(key);

        this.mailPredicate = mailPredicate;
        this.settings = settings;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String mail = args.next();
        if (!mailPredicate.test(mail)) {
            throw args.createError(settings.getText().getNotMail());
        }

        return mail;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("user@example.com");
    }
}
