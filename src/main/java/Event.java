public class Event extends Task {
    private TaskDateTime startDate;
    private TaskDateTime endDate;

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
