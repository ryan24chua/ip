package myriad.task;

import java.time.LocalDateTime;

/**
 * A task that spans a start and an end date/time. Unlike a {@link Deadline},
 * which occurs at one point, an {@code Event} covers everything between its
 * two endpoints, which is what {@link #overlaps} compares against.
 */
public class Event extends Task {

    /** When the event starts. */
    private final TaskDateTime start;

    /** When the event ends; not checked to be after {@link #start}. */
    private final TaskDateTime end;

    /**
     * Creates a not-done {@code Event}. The two times are taken as given:
     * they are not checked for {@code start} being before {@code end}, since
     * the {@code Parser} accepts whatever the user typed.
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
    public TaskType getType() {
        return TaskType.EVENT;
    }

    /**
     * Returns this task's save-format line prefixed with its type code and
     * suffixed with the raw start and end times, so the type and both times
     * can be recovered when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("%s | %s | %s | %s",
                getType().getCode(), super.toSaveFormat(), start.toSaveFormat(), end.toSaveFormat());
    }

    /**
     * Returns the display form, e.g.
     * {@code [E][ ] meeting (from: Dec 02 2019 1400 to: Dec 02 2019 1600)},
     * with both times in {@link TaskDateTime}'s display format, not the save
     * format.
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(), start, end);
    }

    /**
     * Matches if the period from {@code from} to {@code to} overlaps this
     * event's span, from the earliest instant of {@link #start} to the latest
     * instant of {@link #end}. See {@link TaskDateTime#rangesOverlap} for the
     * general rule.
     */
    @Override
    public boolean overlaps(LocalDateTime from, LocalDateTime to) {
        return TaskDateTime.rangesOverlap(
                start.rangeStart(), end.rangeEnd(), from, to);
    }
}
