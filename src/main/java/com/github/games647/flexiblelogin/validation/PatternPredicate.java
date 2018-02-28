package com.github.games647.flexiblelogin.validation;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PatternPredicate implements Predicate<String> {

    private final Pattern pattern;

    public PatternPredicate(String regEx) {
        this.pattern = Pattern.compile(regEx);
    }

    @Override
    public boolean test(String input) {
        return pattern.matcher(input).matches();
    }
}
