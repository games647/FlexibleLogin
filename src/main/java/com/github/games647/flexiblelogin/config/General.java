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

import com.github.games647.flexiblelogin.PomData;
import com.google.common.collect.Lists;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import org.spongepowered.api.service.permission.Subject;

@SuppressWarnings("FieldMayBeFinal")
public class General {

    @Setting(comment = "Database configuration")
    private SQLConfig sqlConfiguration = new SQLConfig();

    @Setting(comment = "Email configuration for password recovery")
    private EmailConfig emailConfiguration = new EmailConfig();

    @Setting(comment = "Algorithms for hashing user passwords. You can also choose totp")
    private HashingAlgorithm hashAlgo = HashingAlgorithm.BCrypt;

    @Setting(comment = "Regular expression for verifying validate player names. Default is a-zA-Z with 2-16 length")
    private String validNames = "^\\w{2,16}$";

    @Setting(comment = "Should the player name always be case sensitive equal to the time the player registered?")
    private boolean caseSensitiveNameCheck = true;

    @Setting(comment = "Should the plugin login users automatically if it's the same account from the same IP")
    private boolean ipAutoLogin;

    @Setting(comment = "Should only the specified commands be protected from unauthorized access")
    private boolean commandOnlyProtection;

    @Setting(comment = "The user should use a strong password")
    private int minPasswordLength = 4;

    @Setting(comment = "Number of seconds a player has time to login or will be kicked.-1 deactivates this features")
    private int timeoutLogin = 60;

    @Setting(comment = "Should this plugin check for player permissions")
    private boolean playerPermissions;

    @Setting(comment = "Teleport the player to a safe location based on the last login coordinates")
    private boolean safeLocation;

    @Setting(comment = "Should the plugin save the login status to the database")
    private boolean updateLoginStatus;

    @Setting(comment = "Do you allow your users to skip authentication with the bypass permission")
    private boolean bypassPermission;

    @Setting(comment = "How many login attempts are allowed until everything is blocked")
    private int maxAttempts = 3;

    @Setting(comment = "How seconds the user should wait after the user tried to make too many attempts")
    private int waitTime = 300;

    @Setting(comment = "Custom command that should run after the user tried to make too many attempts")
    private String lockCommand = "";

    @Setting(comment = "How many accounts are allowed per ip-address. Use 0 to disable it")
    private int maxIpReg;

    @Setting(comment = "Interval where the please login will be printed to the user")
    private int messageInterval = 2;

    @Setting(comment = "Should unregistered player be able to join the server?")
    private boolean allowUnregistered = true;

    @Setting
    private TeleportConfig teleportConfig = new TeleportConfig();

    @Setting(comment = "If command only protection is enabled, these commands are protected. If the list is empty"
            + " all commands are protected")
    private List<String> protectedCommands = Lists.newArrayList("op", "pex");

    public EmailConfig getEmail() {
        return emailConfiguration;
    }

    public SQLConfig getSQL() {
        return sqlConfiguration;
    }

    public HashingAlgorithm getHashAlgo() {
        return hashAlgo;
    }

    public boolean isIpAutoLogin() {
        return ipAutoLogin;
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public boolean isCommandOnlyProtection() {
        return commandOnlyProtection;
    }

    public int getTimeoutLogin() {
        return timeoutLogin;
    }

    public boolean isUpdateLoginStatus() {
        return updateLoginStatus;
    }

    public boolean isPlayerPermissions() {
        return playerPermissions;
    }

    public boolean isBypassed(Subject subject) {
        return bypassPermission && subject.hasPermission(PomData.ARTIFACT_ID + ".bypass");
    }

    public TeleportConfig getTeleport() {
        return teleportConfig;
    }

    public List<String> getProtectedCommands() {
        return protectedCommands;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getMaxIpReg() {
        if (maxIpReg <= 0) {
            return Integer.MAX_VALUE;
        }

        return maxIpReg;
    }

    public String getValidNames() {
        return validNames;
    }

    public boolean isSafeLocation() {
        return safeLocation;
    }

    public String getLockCommand() {
        return lockCommand;
    }

    public int getMessageInterval() {
        return messageInterval;
    }

    public boolean isCaseSensitiveNameCheck() {
        return caseSensitiveNameCheck;
    }

    public boolean isAllowUnregistered() {
        return allowUnregistered;
    }

    @ConfigSerializable
    public enum HashingAlgorithm {

        BCrypt,

        TOTP
    }
}
