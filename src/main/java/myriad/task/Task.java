package myriad.task;

/**
 * Represents a task with a description and a done/not-done status.
 */
public class Task {
    /** Whether the user has marked this task done. Starts false. */
    protected boolean isDone = false;

    /** What the task is, exactly as the user typed it. */
    protected String description;

    /**
     * Creates a task with the given description. The task starts not done.
     *
     * @param description what the task is.
     */
    public Task(String description) {
        this.description = description;
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
     * command. Tasks with no date of their own (ToDo) never match, hence
     * the default false here; Deadline and Event override this with their
     * own date-based check.
     *
     * @param query the date, or date and time, being asked about.
     * @return whether this task overlaps query; always false for a plain Task.
     */
    public boolean occursDuring(TaskDateTime query) {
        return false;
    }

    /**
     * Returns whether this task's description contains keyword, ignoring
     * case, for the "find" command. Only the description is searched, not
     * the type marker or any dates, so "find 2019" won't match a deadline
     * merely because it falls in that year.
     *
     * @param keyword the text to look for within the description.
     * @return whether the description contains keyword, ignoring case.
     */
    public boolean descriptionContains(String keyword) {
        return description.toLowerCase().contains(keyword.toLowerCase());
    }

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
