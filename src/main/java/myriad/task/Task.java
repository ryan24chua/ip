package myriad.task;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Represents a task with a description and a done/not-done status.
 */
public abstract class Task {
    /** Whether the user has marked this task done. Starts false. */
    private boolean isDone;

    /** What the task is, exactly as the user typed it. */
    private final String description;

    /**
     * Creates a task with the given description. The task starts not done.
     *
     * @param description what the task is.
     */
    public Task(String description) {
        // Only non-null is assumed: a hand-edited save file may hold an empty description.
        assert description != null : "Parser and Storage always pass a description string";
        this.description = description;
    }

    /**
     * Returns whether this task is done.
     *
     * @return whether this task is done.
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * Sets whether this task is done. Used both by the mark/unmark
     * commands and by Storage when restoring a task's saved status.
     *
     * @param isDone true to mark done, false to mark not done yet.
     */
    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    /**
     * Returns this task's data as a "|"-delimited line for saving to disk,
     * e.g. "1 | read book" for a done task. Deliberately separate from
     * toString(): that method's bracket-and-icon format is for display and
     * may change independently, whereas this format needs to stay stable
     * and parseable so a saved line can be read back into a Task later.
     * Subclasses prepend their type letter and any extra fields.
     *
     * @return one line of save format, without a trailing newline.
     */
    public String toSaveFormat() {
        return String.format("%d | %s", isDone ? 1 : 0, description);
    }

    /**
     * Returns whether this task occurs during query, for the "show task"
     * command. A date-only query stands for its whole day, so it matches
     * anything overlapping that day. Defined once here in terms of
     * overlaps, and final, so the "show" and "stats" commands can never
     * disagree about when a task takes place.
     *
     * @param query the date, or date and time, being asked about.
     * @return whether this task overlaps query; always false for a ToDo.
     */
    public final boolean occursDuring(TaskDateTime query) {
        return overlaps(query.rangeStart(), query.rangeEnd());
    }

    /**
     * Returns whether this task overlaps the period from from to to, for
     * the "stats" command's due-soon count. Tasks with no date of their own
     * (ToDo) never match, hence the default false here; Deadline and Event
     * override this with their own date-based check.
     *
     * @param from the start of the period.
     * @param to   the end of the period.
     * @return whether this task overlaps that period; always false for a plain Task.
     */
    public boolean overlaps(LocalDateTime from, LocalDateTime to) {
        return false;
    }

    /**
     * Returns whether this task's description contains keyword, ignoring
     * case, for the "find" command. Only the description is searched, not
     * the type marker or any dates, so "find 2019" won't match a deadline
     * merely because it falls in that year.
     *
     * Both sides are lower-cased with Locale.ROOT rather than the machine's
     * default locale, whose rules can differ: in Turkish, for example, "I"
     * lower-cases to a dotless "ı", so "find TITLE" would miss "title".
     *
     * @param keyword the text to look for within the description.
     * @return whether the description contains keyword, ignoring case.
     */
    public boolean descriptionContains(String keyword) {
        return description.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns which kind of task this is, e.g. TaskType.TODO for a ToDo.
     *
     * @return this task's type.
     */
    public abstract TaskType getType();

    /**
     * Returns the task's status and description, e.g. [X] read book
     * if done, or [ ] read book if not done.
     */
    @Override
    public String toString() {
        if (isDone) {
            return String.format("[X] %s", this.description);
        } else {
            return String.format("[ ] %s", this.description);
        }
    }
}
