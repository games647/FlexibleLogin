/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin.config;

import com.google.common.collect.ImmutableMap;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.text.Text.builder;
import static org.spongepowered.api.text.TextTemplate.arg;
import static org.spongepowered.api.text.TextTemplate.of;

@ConfigSerializable
public class TextConfig {

    private static final TextColor WARNING_COLOR = TextColors.DARK_RED;
    private static final TextColor INFO_COLOR = TextColors.DARK_GREEN;

    @Setting(comment = "When a non-player (i.e. Console, Command Block) tries to do a player only action.")
    private Text playersOnly = builder("Only players can do this!").color(WARNING_COLOR).build();

    @Setting(comment = "When the account does not exist on the account database.")
    private Text accountNotLoaded = builder("Your account cannot be loaded.").color(INFO_COLOR).build();

    @Setting(comment = "If the player is logged in, it is then pointless to use the forgot password command")
    private Text alreadyLoggedIn = builder("You are already logged in!").color(WARNING_COLOR).build();

    @Setting(comment = "When the player did not or forgot to submit an email address used to recover a password.")
    private Text uncommittedEmailAddress = builder("You did not submit an email address!").color(WARNING_COLOR).build();

    @Setting(comment = "When an unexpected error occurs. (Should not happen)")
    private Text errorExecutingCommand = builder("Error executing command, see console.").color(WARNING_COLOR).build();

    @Setting(comment = "Whe the player successfully logs out of his/her account.")
    private Text loggedOut = builder("Logged out.").color(INFO_COLOR).build();

    @Setting(comment = "When the player is not logged in of his/her account.")
    private Text notLoggedIn = builder("Not logged in. Type /login to login in").color(WARNING_COLOR).build();

    @Setting(comment = "When the player is not logged in of his/her account.")
    private Text notRegistered = builder("Not registered. Type /register to register").color(WARNING_COLOR).build();

    @Setting(comment = "When totp is not enabled.")
    private Text totpNotEnabled = builder("Totp is not enabled. You have to enter two passwords.")
            .color(WARNING_COLOR).build();

    @Setting(comment = "When the two passwords typed do not match each other.")
    private Text unequalPasswords = builder("The passwords are not equal.").color(WARNING_COLOR).build();

    @Setting(comment = "When the player successfully used the set email command and set his/her email.")
    private Text emailSet = builder("Your email was set.").color(INFO_COLOR).build();

    @Setting(comment = "When the player enters an email that does not exist.")
    private Text notEmail = builder("You have entered in an invalid email!").color(WARNING_COLOR).build();

    @Setting(comment = "When the unregister process failed.")
    private Text unregisterFailed = builder("Your request is neither a player name or uuid.")
            .color(WARNING_COLOR).build();

    @Setting(comment = "When a player successfully logs in.")
    private Text loggedIn = builder("Logged in").color(INFO_COLOR).build();

    @Setting(comment = "When a player enters an incorrect password.")
    private Text incorrectPassword = builder("Incorrect password").color(WARNING_COLOR).build();

    @Setting(comment = "When the recovery email was sent!")
    private Text mailSent = builder("Email sent").color(INFO_COLOR).build();

    @Setting(comment = "When a player's account does not exist.")
    private Text accountNotFound = builder("Account not found").color(WARNING_COLOR).build();

    @Setting(comment = "When a player joined with a non Mojang valid username")
    private Text invalidUsername = builder(
            "Invalid username - Choose characters a-z,A-Z,0-9 and a length between 2 and 16"
    ).color(WARNING_COLOR).build();

    @Setting(comment = "When an account was successfully deleted")
    private TextTemplate accountDelete = of(
            INFO_COLOR, "Deleted account of ", TextColors.YELLOW, arg("account").optional(), "!"
    );

    @Setting(comment = "Kick message if the case sensitive compare between the already registered " +
            "and the joining player failed")
    private TextTemplate invalidCase = of(
            WARNING_COLOR, "Invalid username. Please join as ", TextColors.YELLOW, arg("username").optional(), "!"
    );

    @Setting(comment = "Kick message if the case sensitive compare between the already registered " +
            "and the joining player failed")
    private TextTemplate lastOnline = of(
            INFO_COLOR, "Account: ", TextColors.YELLOW, arg("username").optional(),
            INFO_COLOR, " was last online at ", TextColors.YELLOW, arg("time")
    );

    @Setting(comment = "When an account already exists, and therefore cannot be created.")
    private Text accountAlreadyExists = builder("Account already exists").color(WARNING_COLOR).build();

    @Setting(comment = "When the player successfully created his/her account.")
    private Text accountCreated = builder("Account created").color(INFO_COLOR).build();

    @Setting(comment = "When a secret-key is created (header).")
    private TextTemplate keyGenerated = of(
            INFO_COLOR, "SecretKey generated: ", TextColors.YELLOW, arg("code")
    );

    @Setting(comment = "When a player registered using TOTP and the code can be scanned by clicking on it")
    private Text scanQr = builder("Click here to scan the QR-Code").color(TextColors.YELLOW).build();

    @Setting(comment = "When the user tries to execute a protected command if command only protection is enabled")
    private Text protectedCommand = builder("This command is protected. Please login").color(WARNING_COLOR).build();

    @Setting(comment = "When the player is auto logged in by using the same ip as the last login")
    private Text ipAutoLogin = builder("Auto logged in").color(INFO_COLOR).build();

    @Setting(comment = "Kick message if the player doesn't logged during the configured time out seconds")
    private Text timeoutReason = builder("Login timeout").color(WARNING_COLOR).build();

    @Setting(comment = "Message if the player changed his account password successfully")
    private Text changePassword = builder("Successful changed password").color(INFO_COLOR).build();

    @Setting(comment = "Message if the player has to register with a longer password")
    private Text tooShortPassword = builder("Your password is too short").color(INFO_COLOR).build();

    @Setting(comment = "User reached max attempts")
    private Text maxAttempts = builder("You entered too many times a wrong password").color(WARNING_COLOR).build();

    @Setting(comment = "User reached the max ip registrations")
    private Text maxIpReg = builder("You reached the max amount of registrations for this ip-address")
            .color(WARNING_COLOR).build();

    @Setting(comment = "Admin reloaded the plugin")
    private Text onReload = builder("Successful reloaded plugin").color(INFO_COLOR).build();

    @Setting(comment = "Force register failed because the player is online")
    private Text forceRegisterOnline = builder("Cannot force register player. That player is online")
            .color(INFO_COLOR).build();

    @Setting(comment = "Successfull force registered an account")
    private Text forceRegisterSuccess = builder("Force register success").color(INFO_COLOR).build();

    @Setting(comment = "Another player with the same name tried to join the server while that player is still online")
    private Text alreadyOnline = builder("You are already online").color(INFO_COLOR).build();

    @Setting(comment = "If email recovery is not enabled")
    private Text emailNotEnabled = builder("Email recovery is not enabled.").color(INFO_COLOR).build();

    @Setting(comment = "If unregistered player shouldn't join the server")
    private Text unregisteredKick = builder("You cannot connect because you have to register on the website.")
            .color(INFO_COLOR).build();

    public Text getPlayersOnly() {
        return playersOnly;
    }

    public Text getAccountNotLoaded() {
        return accountNotLoaded;
    }

    public Text getAlreadyLoggedIn() {
        return alreadyLoggedIn;
    }

    public Text getUncommittedEmailAddress() {
        return uncommittedEmailAddress;
    }

    public Text getErrorExecutingCommand() {
        return errorExecutingCommand;
    }

    public Text getLoggedOut() {
        return loggedOut;
    }

    public Text getNotLoggedIn() {
        return notLoggedIn;
    }

    public Text getNotRegistered() {
        return notRegistered;
    }

    public Text getTotpNotEnabled() {
        return totpNotEnabled;
    }

    public Text getUnequalPasswords() {
        return unequalPasswords;
    }

    public Text getEmailSet() {
        return emailSet;
    }

    public Text getNotEmail() {
        return notEmail;
    }

    public Text getUnregisterFailed() {
        return unregisterFailed;
    }

    public Text getLoggedIn() {
        return loggedIn;
    }

    public Text getIncorrectPassword() {
        return incorrectPassword;
    }

    public Text getMailSent() {
        return mailSent;
    }

    public Text getAccountNotFound() {
        return accountNotFound;
    }

    public Text getInvalidUsername() {
        return invalidUsername;
    }

    public Text getAccountAlreadyExists() {
        return accountAlreadyExists;
    }

    public Text getAccountCreated() {
        return accountCreated;
    }

    public Text getScanQr() {
        return scanQr;
    }

    public Text getProtectedCommand() {
        return protectedCommand;
    }

    public Text getIpAutoLogin() {
        return ipAutoLogin;
    }

    public Text getTimeoutReason() {
        return timeoutReason;
    }

    public Text getChangePassword() {
        return changePassword;
    }

    public Text getTooShortPassword() {
        return tooShortPassword;
    }

    public Text getMaxAttempts() {
        return maxAttempts;
    }

    public Text getMaxIpReg() {
        return maxIpReg;
    }

    public Text getOnReload() {
        return onReload;
    }

    public Text getForceRegisterOnline() {
        return forceRegisterOnline;
    }

    public Text getForceRegisterSuccess() {
        return forceRegisterSuccess;
    }

    public Text getAlreadyOnline() {
        return alreadyOnline;
    }

    public Text getEmailNotEnabled() {
        return emailNotEnabled;
    }

    public Text getUnregisteredKick() {
        return unregisteredKick;
    }

    public Text getAccountDeleted(String account) {
        return accountDelete.apply(ImmutableMap.of("account", account)).build();
    }

    public Text getKeyGenerated(String code) {
        return keyGenerated.apply(ImmutableMap.of("code", code)).build();
    }

    public Text getInvalidCase(String username) {
        return invalidCase.apply(ImmutableMap.of("username", username)).build();
    }

    public Text getLastOnline(String username, String time) {
        return lastOnline.apply(ImmutableMap.of("username", username, "time", time)).build();
    }
}
