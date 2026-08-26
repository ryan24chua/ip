package myriad.task;

/**
 * Represents a task with a description and a done/not-done status.
 */
public class Task {
    protected boolean isDone = false;
    protected String description;

    /**
     * Creates a task with the given description. The task starts not done.
     */
    public Task(String description) {
        this.description = description;
    }

    /**
     * Sets whether this task is done.
     */
    public void setDone(boolean done) {
        this.isDone = done;
    }

    /**
     * Returns this task's data as a "|"-delimited line for saving to disk,
     * e.g. "1 | read book" for a done task. Deliberately separate from
     * toString(): that method's bracket-and-icon format is for display and
     * may change independently, whereas this format needs to stay stable
     * and parseable so a saved line can be read back into a Task later.
     * Subclasses prepend their type letter and any extra fields.
     */
    public String toSaveFormat() {
        return String.format("%d | %s", isDone ? 1 : 0, description);
    }

    /**
     * Returns whether this task occurs during query, for the "show task"
     * command. Tasks with no date of their own (ToDo) never match, hence
     * the default false here; Deadline and Event override this with their
     * own date-based check.
     */
    public boolean occursDuring(TaskDateTime query) {
        return false;
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
