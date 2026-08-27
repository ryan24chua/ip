package myriad.task;

/**
 * A task with a description and nothing else — no date of its own, so it
 * never matches the "show" command (Task.occursDuring stays false).
 */
public class ToDo extends Task {

    /**
     * Creates a not-done ToDo with the given description.
     *
     * @param description what the user typed after "todo".
     */
    public ToDo(String description) {
        super(description);
    }

    /**
     * Returns this task's save-format line prefixed with "T" so it can be
     * recognized as a ToDo when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("T | %s", super.toSaveFormat());
    }

    /**
     * Returns the display form, e.g. [T][ ] read book — the "[T]" tag marks
     * it as a ToDo, the rest is Task's own status-and-description form.
     */
    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }
}
