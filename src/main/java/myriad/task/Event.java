package myriad.task;

import java.time.LocalDateTime;

/**
 * A task that spans a start and an end date/time. Unlike a Deadline, which
 * occurs at one point, an Event covers everything between its two
 * endpoints — which is what occursDuring below compares against.
 */
public class Event extends Task {

    /** Leading field of an Event's save-format line; Storage reads it back. */
    public static final String TYPE_CODE = "E";

    /** When the event starts. */
    private final TaskDateTime start;

    /** When the event ends; not checked to be after start. */
    private final TaskDateTime end;

    /**
     * Creates a not-done Event. The two times are taken as given: they are
     * not checked for start being before end, since the Parser accepts
     * whatever the user typed.
     *
     * @param description what the event is.
     * @param start       when it starts, already parsed.
     * @param end         when it ends, already parsed.
     */
    public Event(String description, TaskDateTime start, TaskDateTime end) {
        super(description);
        // Start before end is deliberately not asserted: that comes from user input.
        assert start != null && end != null : "TaskDateTime.parse never returns null";

        this.start = start;
        this.end = end;
    }

    @Override
    public String getTypeCode() {
        return TYPE_CODE;
    }

    /**
     * Returns this task's save-format line prefixed with TYPE_CODE and
     * suffixed with the raw start and end times, so the type and both times
     * can be recovered when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("%s | %s | %s | %s",
                TYPE_CODE, super.toSaveFormat(), start.toSaveFormat(), end.toSaveFormat());
    }

    /**
     * Returns the display form, e.g.
     * [E][ ] meeting (from: Dec 02 2019 1400 to: Dec 02 2019 1600) — both
     * times in TaskDateTime's display format, not the save format.
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(), start, end);
    }

    /**
     * Matches if query overlaps this event's span, from start's
     * earliest instant to end's latest instant — see
     * TaskDateTime.rangesOverlap for the general rule.
     */
    @Override
    public boolean occursDuring(TaskDateTime query) {
        return TaskDateTime.rangesOverlap(
                start.rangeStart(), end.rangeEnd(), query.rangeStart(), query.rangeEnd());
    }

    /**
     * Matches if the period from from to to overlaps this event's span,
     * from start's earliest instant to end's latest instant — see
     * TaskDateTime.rangesOverlap for the general rule.
     */
    @Override
    public boolean overlaps(LocalDateTime from, LocalDateTime to) {
        return TaskDateTime.rangesOverlap(
                start.rangeStart(), end.rangeEnd(), from, to);
    }
}
