package com.github.games647.flexiblelogin.validation;

public class UUIDPredicate extends PatternPredicate {

    private static final String UUID_PATERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";

    public UUIDPredicate() {
        super(UUID_PATERN);
    }
}
