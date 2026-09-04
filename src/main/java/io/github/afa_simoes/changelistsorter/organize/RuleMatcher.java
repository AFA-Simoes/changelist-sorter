package io.github.afa_simoes.changelistsorter.organize;

import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pure, platform-state-free rule matcher. Compiles every enabled, non-blank rule's wildcard
 * pattern exactly once at construction time, then matches file names/paths against them.
 */
public class RuleMatcher {
    private final List<CompiledRule> rules;

    public RuleMatcher(List<ChangelistOrganizerItem> items) {
        List<CompiledRule> compiled = new ArrayList<>();

        for (ChangelistOrganizerItem item : items) {
            if (!item.isEnabled()) {
                continue;
            }

            String changeListName = item.getChangeListName();
            if (changeListName == null || changeListName.trim().isEmpty()) {
                continue;
            }

            String filePattern = item.getFilePattern();
            if (filePattern == null || filePattern.trim().isEmpty()) {
                continue;
            }

            compiled.add(new CompiledRule(item, Pattern.compile(toRegex(filePattern))));
        }

        this.rules = compiled;
    }

    /**
     * Matches a single file (identified by its name and, when needed, its project-relative or
     * absolute path) against the compiled rules, in rule order.
     *
     * @param stopAfterFirstMatch when {@code true}, returns at most one match; otherwise
     *                            returns every matching rule, in order.
     */
    public List<ChangelistOrganizerItem> match(String fileName, String fullPath, boolean stopAfterFirstMatch) {
        List<ChangelistOrganizerItem> matches = new ArrayList<>();

        for (CompiledRule rule : rules) {
            String compareValue = rule.item.isCheckFullPath() ? fullPath : fileName;

            if (rule.pattern.matcher(compareValue).matches()) {
                matches.add(rule.item);

                if (stopAfterFirstMatch) {
                    break;
                }
            }
        }

        return matches.isEmpty() ? Collections.emptyList() : matches;
    }

    /**
     * Translates a user-facing wildcard pattern ({@code *} = any run of characters, {@code ?} =
     * exactly one character) into a regex, escaping every other character so it is matched
     * literally - including regex metacharacters such as {@code . ( ) [ ] + $ ^ { } |} and
     * backslashes from Windows-style paths.
     */
    static String toRegex(String mask) {
        StringBuilder regex = new StringBuilder(mask.length() + 16);

        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);

            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append('.');
                    break;
                default:
                    if ("\\.^$|+()[]{}".indexOf(c) >= 0) {
                        regex.append('\\');
                    }
                    regex.append(c);
                    break;
            }
        }

        return regex.toString();
    }

    private static final class CompiledRule {
        private final ChangelistOrganizerItem item;
        private final Pattern pattern;

        private CompiledRule(ChangelistOrganizerItem item, Pattern pattern) {
            this.item = item;
            this.pattern = pattern;
        }
    }
}
