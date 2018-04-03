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
package com.github.games647.flexiblelogin.hasher;

import com.google.common.primitives.Ints;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey.Builder;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.warrenstrange.googleauth.HmacHashFunction;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;

public class TOTP implements Hasher {

    private final GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfigBuilder()
            .setHmacHashFunction(HmacHashFunction.HmacSHA512)
            //HmacSHA512 is not yet compatible with Base64
            .setKeyRepresentation(KeyRepresentation.BASE64)
            .build();

    private final IGoogleAuthenticator gAuth = new GoogleAuthenticator(config);

    public String getGoogleBarcodeURL(String user, String host, String secret) {
        //this returns the google chart URL
        GoogleAuthenticatorKey key = new Builder(secret).setConfig(config).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(user, host, key);
    }

    @Override
    public String hash(String rawPassword) {
        //it's the secret code. We need to save it here
        return gAuth.createCredentials().getKey();
    }

    @Override
    public boolean checkPassword(String passwordHash, String userInput) {
        Integer code = Ints.tryParse(userInput);
        return code != null && gAuth.authorize(passwordHash, code);
    }
}
