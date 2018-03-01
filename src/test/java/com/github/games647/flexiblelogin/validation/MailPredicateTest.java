package com.github.games647.flexiblelogin.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailPredicateTest {

    private MailPredicate predicate;

    @BeforeEach
    void setUp() {
        predicate = new MailPredicate();
    }

    @Test
    void testValidMail() {
        assertTrue(predicate.test("user@gmail.com"));
    }

    @Test
    void testInvalid() {
        assertFalse(predicate.test("user@"));
    }

    @Test
    void testSubdomain() {
        assertTrue(predicate.test("user@bla.gmail.com"));
    }

    @Test
    void testExplicitFolder() {
        assertTrue(predicate.test("user+folder@bla.gmail.com"));
    }
}
