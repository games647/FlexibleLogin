package com.github.games647.flexiblelogin.config;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

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

    @Setting(comment = "When the player successfully created his/her account.")
    private String playersAccountCreated = "§2Account created.";

    @Setting(comment = "When an account already exists, and therefore cannot be created.")
    private String playersAccountExists = "§4Account already exists!";

    @Setting(comment = "When a player's account was not found!")
    private String playersAccountNotFound = "&4Account not found!";

    @Setting(comment = "When the player did not or forgot to submit an email address used to recover a password.")
    private String uncommittedEmailAddress = "§4You did not submit an email address!";

    @Setting(comment = "When there is a (usually uncalled for) exception in the plugin.")
    private String errorExecutingCommand = "§4Error executing command, see console.";

    @Setting(comment = "Whe the player successfully logs out of his/her account.")
    private String loggedOut = "§2Logged out.";

    @Setting(comment = "When the player is not logged in of his/her account.")
    private String notLoggedIn = "§4Not logged in.";

    @Setting(comment = "When a player successfully logs in.")
    private String loggedIn = "§2Logged in";

    @Setting(comment = "When totp is not enabled.")
    private String totpNotEnabled = "§4Totp is not enabled. You have to enter two passwords.";

    @Setting(comment = "When the two passwords typed do not match each other.")
    private String unevenPasswords = "§4The passwords are not equal.";

    @Setting(comment = "When the player successfully used the set email command and set his/her email.")
    private String emailSet = "§2Your email was set.";

    @Setting(comment = "When the player enters an email that does not exist.")
    private String notEmail = "§4You have entered in an invalid email!";

    @Setting(comment = "When the email was sent!")
    private String emailSent = "&2The email has been sent!";

    @Setting(comment = "When the unregister process failed.")
    private String unregisterFailed = "§4Your request is neither a player name or uuid.";

    @Setting(comment = "When a player's account does not exist.")
    private String accountDoesNotExist = "§4Your account does not exist!";

    @Setting(comment = "When a player enters an incorrect password.")
    private String incorrectPassword = "§4Your password is incorrect!";

    @Setting(comment = "When an unexpected error occurs. (Should not happen)")
    private String unexpectedError = "§4An unexpected exception has occured. Please check console for details.";

    @Setting(comment = "When a secretkey is created (header).")
    private String secretKeyCreatedHeader = "§2SecretKey created:";

    @Setting(comment = "When a secretkey is created.")
    private String secretKeyCreated = "&l(secretkey) or click &lHERE &rto scan the url. (url)";

    @Setting(comment = "When an account was successfully deleted.")
    private String accountDeleted = "§2Deleted account of: (identifier)";

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

    public Text getAccountCreatedMessage() {
        return fromString(playersAccountCreated);
    }

    public Text getAccountExistsMessage() {
        return fromString(playersAccountExists);
    }

    public Text getAccountNotFoundMessage() {
        return fromString(playersAccountNotFound);
    }

    public Text getAccountDeletedMessage(String identifier) {
        return fromString(accountDeleted.replaceAll("\\(identifier\\)", identifier));
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

    public Text getLoggedInMessage() {
        return fromString(loggedIn);
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

    public Text getEmailSentMessage() {
        return fromString(emailSent);
    }

    public Text getUnregisteringFailedMessage() {
        return fromString(unregisterFailed);
    }

    public Text getAccountDoesNotExistMessage() {
        return fromString(accountDoesNotExist);
    }

    public Text getIncorrectPasswordMessage() {
        return fromString(incorrectPassword);
    }

    public Text getUnexpectedErrorMessage() {
        return fromString(unexpectedError);
    }

    public Text getSecretKeyCreatedMessageHeader() {
        return fromString(secretKeyCreatedHeader);
    }

    public Text getSecretKeyCreatedMessage(String key, String url) {
        return fromString(secretKeyCreated.replaceAll("\\(secretkey\\)", key).replaceAll("\\(url\\)", url));
    }

    private Text fromString(String textString) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(textString);
    }
}
