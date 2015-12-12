package com.github.games647.flexiblelogin.config;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class TextConfiguration {

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

    @Setting(comment = "If the player is logged in, it is then pointless to use the forgot password command")
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
        return fromString(playersOnly);
    }

    public Text getPlayersOnlyRecoverMessage() {
        return fromString(playersOnlyPassword);
    }

    public Text getPlayersOnlyLogoutMessage() {
        return fromString(playersOnlyLogout);
    }

    public Text getPlayersOnlyRegisterMessage() {
        return fromString(playersOnlyRegister);
    }

    public Text getPlayersOnlySetEmail() {
        return fromString(playersOnlySetEmail);
    }

    public Text getAccountNotLoadedMessage() {
        return fromString(playersAccountNotLoaded);
    }

    public Text getAlreadyLoggedInMessage() {
        return fromString(playersAccountAlreadyLoggedIn);
    }

    public Text getUncommittedEmailAddressMessage() {
        return fromString(uncommittedEmailAddress);
    }

    public Text getErrorCommandMessage() {
        return fromString(errorExecutingCommand);
    }

    public Text getSuccessfullyLoggedOutMessage() {
        return fromString(loggedOut);
    }

    public Text getNotLoggedInMessage() {
        return fromString(notLoggedIn);
    }

    public Text getTotpNotEnabledMessage() {
        return fromString(totpNotEnabled);
    }

    public Text getUnequalPasswordsMessage() {
        return fromString(unevenPasswords);
    }

    public Text getEmailSetMessage() {
        return fromString(emailSet);
    }

    public Text getNotEmailMessage() {
        return fromString(notEmail);
    }

    public Text getUnregisteringFailedMessage() {
        return fromString(unregisterFailed);
    }

    private Text fromString(String textString) {
        return Texts.legacy().fromUnchecked(textString);
    }
}
