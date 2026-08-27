package myriad.task;

/**
 * A task that has to be done by one date, optionally with a time of day.
 * The due date is kept as a TaskDateTime rather than raw text, so that
 * "show" can compare it against a queried date/time instead of matching
 * strings.
 */
public class Deadline extends Task {
    private TaskDateTime date;

    /**
     * Creates a not-done Deadline.
     *
     * @param description what the task is.
     * @param date        when it is due, already parsed.
     */
    public Deadline(String description, TaskDateTime date) {
        super(description);

        this.date = date;
    }

    /**
     * Returns this task's save-format line prefixed with "D" and suffixed
     * with the raw due date, so both the type and the date can be
     * recovered when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("D | %s | %s", super.toSaveFormat(), date.toSaveFormat());
    }

    /**
     * Returns the display form, e.g. [D][ ] return book (by: Dec 02 2019) —
     * the date shown in TaskDateTime's display format, not the save format.
     */
    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)", super.toString(), this.date);
    }

    /**
     * Matches if query overlaps this deadline's own instant (or, if the
     * deadline has no time of its own, its whole day) — see
     * TaskDateTime.rangesOverlap for the general rule.
     */
    @Override
    public boolean occursDuring(TaskDateTime query) {
        return TaskDateTime.rangesOverlap(
                date.rangeStart(), date.rangeEnd(), query.rangeStart(), query.rangeEnd());
    }
}
