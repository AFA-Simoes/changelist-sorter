package io.github.afa_simoes.changelistsorter.organize;

import io.github.afa_simoes.changelistsorter.ChangelistOrganizerItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RuleMatcherTest {

    @ParameterizedTest(name = "pattern \"{0}\" against \"{1}\" -> {2}")
    @MethodSource("patternCases")
    void matchesFileNameAgainstPattern(String pattern, String fileName, boolean expectedMatch) {
        ChangelistOrganizerItem item = itemWithPattern(pattern);
        RuleMatcher matcher = new RuleMatcher(List.of(item));

        boolean matched = matcher.match(fileName, "irrelevant", false).contains(item);

        assertEquals(expectedMatch, matched);
    }

    static Stream<Arguments> patternCases() {
        return Stream.of(
                // '*' matches any run of characters
                arguments("*.java", "Main.java", true),
                arguments("*.java", "Main.kt", false),

                // '?' matches exactly one character - not zero-or-one
                arguments("Foo?.txt", "Foo1.txt", true),
                arguments("Foo?.txt", "Foo.txt", false),
                arguments("Foo?.txt", "Foo12.txt", false),

                // regex metacharacters are matched literally, not interpreted
                arguments("a(b).txt", "a(b).txt", true),
                arguments("a[b].txt", "a[b].txt", true),
                arguments("a+b.txt", "a+b.txt", true),
                arguments("a$b.txt", "a$b.txt", true),
                arguments("a^b.txt", "a^b.txt", true),
                arguments("a{b}.txt", "a{b}.txt", true),
                arguments("a|b.txt", "a|b.txt", true),
                arguments("a|b.txt", "a.txt", false)
        );
    }

    @Test
    void checkFullPathMatchesAgainstPathRatherThanName() {
        ChangelistOrganizerItem item = new ChangelistOrganizerItem(true, "target", "src/main/*", true, false);
        RuleMatcher matcher = new RuleMatcher(List.of(item));

        assertTrue(matcher.match("Foo.java", "src/main/Foo.java", false).contains(item));
        assertTrue(matcher.match("src/main/Foo.java", "elsewhere/Foo.java", false).isEmpty());
    }

    @Test
    void backslashesInWindowsPathsAreEscapedNotInterpretedAsRegex() {
        ChangelistOrganizerItem item = new ChangelistOrganizerItem(true, "target", "C:\\Users\\*\\Foo.txt", true, false);
        RuleMatcher matcher = new RuleMatcher(List.of(item));

        assertTrue(matcher.match("Foo.txt", "C:\\Users\\jane\\Foo.txt", false).contains(item));
    }

    @Test
    void disabledRulesAreSkipped() {
        ChangelistOrganizerItem item = new ChangelistOrganizerItem(false, "target", "*.txt", false, false);
        RuleMatcher matcher = new RuleMatcher(List.of(item));

        assertTrue(matcher.match("a.txt", "a.txt", false).isEmpty());
    }

    @Test
    void rulesWithABlankOrNullChangeListNameAreSkipped() {
        ChangelistOrganizerItem blankName = new ChangelistOrganizerItem(true, "  ", "*.txt", false, false);
        ChangelistOrganizerItem nullName = new ChangelistOrganizerItem(true, null, "*.txt", false, false);
        RuleMatcher matcher = new RuleMatcher(List.of(blankName, nullName));

        assertTrue(matcher.match("a.txt", "a.txt", false).isEmpty());
    }

    @Test
    void rulesWithABlankOrNullPatternAreSkipped() {
        ChangelistOrganizerItem blankPattern = new ChangelistOrganizerItem(true, "target", "  ", false, false);
        ChangelistOrganizerItem nullPattern = new ChangelistOrganizerItem(true, "target", null, false, false);
        RuleMatcher matcher = new RuleMatcher(List.of(blankPattern, nullPattern));

        assertTrue(matcher.match("a.txt", "a.txt", false).isEmpty());
    }

    @Test
    void stopAfterFirstMatchReturnsOnlyTheFirstMatchingRule() {
        ChangelistOrganizerItem first = new ChangelistOrganizerItem(true, "first", "*.txt", false, false);
        ChangelistOrganizerItem second = new ChangelistOrganizerItem(true, "second", "*.txt", false, false);
        RuleMatcher matcher = new RuleMatcher(List.of(first, second));

        assertEquals(List.of(first), matcher.match("a.txt", "a.txt", true));
    }

    @Test
    void withoutStopAfterFirstMatchEveryMatchingRuleIsReturnedInOrder() {
        ChangelistOrganizerItem first = new ChangelistOrganizerItem(true, "first", "*.txt", false, false);
        ChangelistOrganizerItem second = new ChangelistOrganizerItem(true, "second", "*.txt", false, false);
        RuleMatcher matcher = new RuleMatcher(List.of(first, second));

        assertEquals(List.of(first, second), matcher.match("a.txt", "a.txt", false));
    }

    private static ChangelistOrganizerItem itemWithPattern(String pattern) {
        return new ChangelistOrganizerItem(true, "target", pattern, false, false);
    }
}
