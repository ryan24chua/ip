package myriad.task;

/**
 * A task that spans a start and an end date/time. Unlike a Deadline, which
 * occurs at one point, an Event covers everything between its two
 * endpoints — which is what occursDuring below compares against.
 */
public class Event extends Task {
    private TaskDateTime startDate;
    private TaskDateTime endDate;

    /**
     * Creates a not-done Event. The two times are taken as given: they are
     * not checked for start being before end, since the Parser accepts
     * whatever the user typed.
     *
     * @param description what the event is.
     * @param startDate   when it starts, already parsed.
     * @param endDate     when it ends, already parsed.
     */
    public Event(String description, TaskDateTime startDate, TaskDateTime endDate) {
        super(description);

        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Returns this task's save-format line prefixed with "E" and suffixed
     * with the raw start and end times, so the type and both times can be
     * recovered when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("E | %s | %s | %s", super.toSaveFormat(), startDate.toSaveFormat(), endDate.toSaveFormat());
    }

    /**
     * Returns the display form, e.g.
     * [E][ ] meeting (from: Dec 02 2019 1400 to: Dec 02 2019 1600) — both
     * times in TaskDateTime's display format, not the save format.
     */
    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(), this.startDate, this.endDate);
    }

    /**
     * Matches if query overlaps this event's span, from startDate's
     * earliest instant to endDate's latest instant — see
     * TaskDateTime.rangesOverlap for the general rule.
     */
    @Override
    public boolean occursDuring(TaskDateTime query) {
        return TaskDateTime.rangesOverlap(
                startDate.rangeStart(), endDate.rangeEnd(), query.rangeStart(), query.rangeEnd());
    }
}
