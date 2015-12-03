package com.github.games647.flexiblelogin.config;

import com.gmail.frogocomics.flexiblelogin.LogUtils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.TextMessageException;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class TextConfiguration {

    private Text parsingTextError = Texts.of(TextStyles.BOLD, TextColors.AQUA, "FlexibleLogin ", TextColors.GRAY, "\u2759 ", TextStyles.NONE, TextColors.DARK_RED, "The text configuration cannot be parsed. Please check the configuration and try again.");

    @Setting(comment = "When a non-player (i.e. Console, Command Block) tries to login.")
    private String playersOnly = "§4Only players need to login!";

    @Setting(comment = "When a non-player tries to recover their password- they don't have one.")
    private String playersOnlyPassword = "§4Only players need to recover their password!";

    @Setting(comment = "When a non-player tries to log out.")
    private String playersOnlyLogout = "§4Only players can log out!";

    @Setting(comment = "When a non-player tries to register.")
    private String playersOnlyRegister = "§4Only players need to register!";

    @Setting(comment = "When a non-player tries to set its email")
    private String playersOnlySetEmail = "§4Only players can set his/her email!";

    @Setting(comment = "When the account does not exist on the account database.")
    private String playersAccountNotLoaded = "§4Your account cannot be loaded.";

    @Setting(comment = "When the player's account is already in: it is then pointless to use the forgot password command")
    private String playersAccountAlreadyLoggedIn = "§4You are already logged in!";

    @Setting(comment = "When the player did not or forgot to submit an email address used to recover a password.")
    private String uncommittedEmailAddress = "§4You did not submit an email address!";

    @Setting(comment = "When there is a (usually uncalled for) exception in the plugin.")
    private String errorExecutingCommand = "§4Error executing command, see console.";

    @Setting(comment = "Whe the player successfully logs out of his/her account.")
    private String loggedOut = "§2Logged out.";

    @Setting(comment = "When the player is not logged in of his/her account.")
    private String notLoggedIn = "§4Not logged in.";

    @Setting(comment = "When totp is not enabled.")
    private String totpNotEnabled = "§4Totp is not enabled. You have to enter two passwords.";

    @Setting(comment = "When the two passwords typed do not match each other.")
    private String unevenPasswords = "§4The passwords are not equal.";

    @Setting(comment = "When the player successfully used the set email command and set his/her email.")
    private String emailSet = "§2Your email was set.";

    @Setting(comment = "When the player enters an email that does not exist.")
    private String notEmail = "§4You have entered in an invalid email!";

    @Setting(comment = "When the unregister process failed.")
    private String unregisterFailed = "§4Your request is neither a player name or uuid.";

    public Text getPlayersOnlyMessage() {
        try {
            return fromString(playersOnly);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersOnlyRecoverPasswordMessage() {
        try {
            return fromString(playersOnlyPassword);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersOnlyLogoutMessage() {
        try {
            return fromString(playersOnlyLogout);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersOnlyRegisterMessage() {
        try {
            return fromString(playersOnlyRegister);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersOnlySetEmail() {
        try {
            return fromString(playersOnlySetEmail);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersAccountNotLoadedMessage() {
        try {
            return fromString(playersAccountNotLoaded);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getPlayersAccountAlreadyLoggedInMessage() {
        try {
            return fromString(playersAccountAlreadyLoggedIn);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getUncommittedEmailAddressMessage() {
        try {
            return fromString(uncommittedEmailAddress);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getErrorExecutingCommandMessage() {
        try {
            return fromString(errorExecutingCommand);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getSuccessfullyLoggedOutMessage() {
        try {
            return fromString(loggedOut);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getNotLoggedInMessage() {
        try {
            return fromString(notLoggedIn);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getTotpNotEnabledMessage() {
        try {
            return fromString(totpNotEnabled);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getUnequalPasswordsMessage() {
        try {
            return fromString(unevenPasswords);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getEmailSetMessage() {
        try {
            return fromString(emailSet);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getNotEmailMessage() {
        try {
            return fromString(notEmail);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    public Text getUnregisteringFailedMessage() {
        try {
            return fromString(unregisterFailed);
        } catch(TextMessageException e) {
            LogUtils.logException(e);
            return parsingTextError;
        }
    }

    private Text fromString(String textString) throws TextMessageException {
        return Texts.legacy().from(textString);
    }
}
