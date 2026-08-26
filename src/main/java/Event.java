public class Event extends Task {
    private String startDate;
    private String endDate;

    public Event(String description, String startDate, String endDate) {
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
        return String.format("E | %s | %s | %s", super.toSaveFormat(), startDate, endDate);
    }

    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)", super.toString(), this.startDate, this.endDate);
    }
}
