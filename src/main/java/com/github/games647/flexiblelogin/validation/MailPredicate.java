package com.github.games647.flexiblelogin.validation;

public class MailPredicate extends PatternPredicate {

    private static final String MAIL_PATTERN = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

    public MailPredicate() {
        super(MAIL_PATTERN);
    }
}
