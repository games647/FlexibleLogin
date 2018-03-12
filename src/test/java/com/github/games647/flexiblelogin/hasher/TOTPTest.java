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

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.HmacHashFunction;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TOTPTest {

    private final IGoogleAuthenticator specImpl = new GoogleAuthenticator(new GoogleAuthenticatorConfigBuilder()
            .setHmacHashFunction(HmacHashFunction.HmacSHA256)
            //HmacSHA512 is not yet compatible with Base64
            .setKeyRepresentation(KeyRepresentation.BASE64)
            .build());

    private TOTP totp;

    @BeforeEach
    void setUp() {
        totp = new TOTP();
    }

    @Test
    void testGenerateKey() {
        //test if generation still works after remove libraries
        assertNotNull(totp.hash(""));
    }

    @Test
    void testCompare() {
        GoogleAuthenticatorKey credentials = specImpl.createCredentials();
        String secretKey = credentials.getKey();
        assertTrue(totp.checkPassword(secretKey, String.valueOf(specImpl.getTotpPassword(secretKey))));
    }

    @Test
    void testNegativeCompare() {
        GoogleAuthenticatorKey credentials = specImpl.createCredentials();
        String secretKey = credentials.getKey();
        assertFalse(totp.checkPassword(secretKey, String.valueOf(specImpl.getTotpPassword(secretKey) + 1)));
    }
}
