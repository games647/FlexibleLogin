package com.github.games647.flexiblelogin.hasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptHasherTest {

    //password=test
    //PHP- generated password
    private static final String TEST_HASH = "$2y$10$dZjLgBbehQa.pDwH4Atzt.5rG0FI54fSyqN3U0GMr1tbMAVPvISr6";

    //standard java jBCrypt generated password to support old versions
    private static final String TEST_HASH_2 = "$2a$10$dZjLgBbehQa.pDwH4Atzt.5rG0FI54fSyqN3U0GMr1tbMAVPvISr6";

    private BcryptHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BcryptHasher();
    }

    @Test
    void correctPrefix() {
        String hash = hasher.hash("test");
        assertEquals('y', hash.charAt(2));
    }

    @Test
    void checkPassword() {
        assertTrue(hasher.checkPassword(TEST_HASH, "test"));
        assertTrue(hasher.checkPassword(TEST_HASH_2, "test"));
    }
}
