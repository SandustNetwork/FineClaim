package com.sandustnetwork.fineclaim.claim.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommandSuggestions {

    private CommandSuggestions() {
    }

    public static List<String> filter(List<String> options, String prefix) {
        String normalizedPrefix = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
