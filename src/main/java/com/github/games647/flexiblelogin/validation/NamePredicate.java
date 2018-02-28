package com.github.games647.flexiblelogin.validation;

import com.github.games647.flexiblelogin.config.Settings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NamePredicate extends PatternPredicate {

    @Inject
    NamePredicate(Settings settings) {
        super(settings.getGeneral().getValidNames());
    }
}
