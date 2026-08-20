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
     * Returns the task's status and description, e.g. {@code [X] read book}
     * if done, or {@code [ ] read book} if not done.
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
