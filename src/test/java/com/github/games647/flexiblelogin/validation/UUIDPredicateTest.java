package com.github.games647.flexiblelogin.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UUIDPredicateTest {

    private UUIDPredicate predicate;

    @BeforeEach
    void setUp() {
        predicate = new UUIDPredicate();
    }

    @Test
    void testValid() {
        assertTrue(predicate.test("0aaa2c13-922a-411b-b655-9b8c08404695"));
    }

    @Test
    void testInvalidWithoutDashes() {
        assertFalse(predicate.test("0aaa2c13922a411bb6559b8c08404695"));
    }

    @Test
    void testEmpty() {
        assertFalse(predicate.test(""));
    }

    @Test
    void testNumber() {
        assertFalse(predicate.test("123"));
    }
}
