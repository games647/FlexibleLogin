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

    private final IGoogleAuthenticator specImplementation = new GoogleAuthenticator(new GoogleAuthenticatorConfigBuilder()
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
        GoogleAuthenticatorKey credentials = specImplementation.createCredentials();
        String secretKey = credentials.getKey();
        assertTrue(totp.checkPassword(secretKey, String.valueOf(specImplementation.getTotpPassword(secretKey))));
    }

    @Test
    void testNegativeCompare() {
        GoogleAuthenticatorKey credentials = specImplementation.createCredentials();
        String secretKey = credentials.getKey();
        assertFalse(totp.checkPassword(secretKey, String.valueOf(specImplementation.getTotpPassword(secretKey) + 1)));
    }
}
