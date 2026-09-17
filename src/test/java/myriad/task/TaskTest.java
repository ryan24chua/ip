package myriad.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import myriad.MyriadException;

/**
 * Tests for {@link Task#descriptionContains}, the match behind the "find"
 * command. Besides ordinary case-insensitive matching, it checks that the
 * result does not depend on the default locale of the machine running it.
 */
public class TaskTest {

    /** Builds a TaskDateTime, unwrapping the checked exception for brevity. */
    private static TaskDateTime at(String raw) {
        try {
            return TaskDateTime.parse(raw);
        } catch (MyriadException e) {
            throw new AssertionError("test fixture should parse: " + raw, e);
        }
    }

    /**
     * Runs the given check with the default locale temporarily set to
     * locale, restoring the original afterwards even if the check fails.
     */
    private static void runWithDefaultLocale(Locale locale, Runnable check) {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(locale);
            check.run();
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void descriptionContains_keywordInDifferentCase_true() {
        assertTrue(new ToDo("Read Book").descriptionContains("rEAD bOOK"));
    }

    @Test
    public void descriptionContains_keywordIsPartOfWord_true() {
        assertTrue(new ToDo("read notebook").descriptionContains("book"));
    }

    @Test
    public void descriptionContains_keywordAbsent_false() {
        assertFalse(new ToDo("read book").descriptionContains("return"));
    }

    @Test
    public void descriptionContains_keywordOnlyInDate_false() {
        // Only the description is searched, not the dates around it.
        Deadline deadline = new Deadline("homework", at("2019-12-02"));
        assertFalse(deadline.descriptionContains("2019"));
    }

    @Test
    public void descriptionContains_turkishDefaultLocale_capitalIStillMatches() {
        // Turkish lower-cases "I" to a dotless "ı", so matching with the
        // default locale would make "TITLE" miss "title".
        Locale turkish = Locale.forLanguageTag("tr-TR");
        runWithDefaultLocale(turkish, () -> assertTrue(new ToDo("write title").descriptionContains("TITLE")));
    }
}
