public class Deadline extends Task {
    private TaskDateTime date;

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

    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)", super.toString(), this.date);
    }
}
