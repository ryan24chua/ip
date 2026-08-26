import java.util.ArrayList;

/**
 * Holds the user's tasks and the operations that mutate them (add,
 * mark done/not done). Has no console I/O of its own — Ui is solely
 * responsible for displaying anything about a TaskList's contents.
 */
public class TaskList {
    private final ArrayList<Task> tasks = new ArrayList<>();

    public void add(Task task) {
        tasks.add(task);
    }

    public Task get(int index) {
        return tasks.get(index);
    }

    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether index is a valid 0-based index into this list.
     */
    public boolean isValidIndex(int index) {
        return index >= 0 && index < tasks.size();
    }

    public void markDone(int index) {
        tasks.get(index).setDone(true);
    }

    public void markNotDone(int index) {
        tasks.get(index).setDone(false);
    }

    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the live, mutable list of tasks — not a defensive copy.
     * Callers that only need to read/display tasks (e.g. Ui) are fine;
     * mutating the returned list bypasses TaskList entirely.
     */
    public ArrayList<Task> asList() {
        return tasks;
    }

    /**
     * Returns a new list of every task whose occursDuring(query) is true,
     * for the "show task" command. Unlike asList(), this is always a fresh
     * list — safe to hand to a caller without exposing the underlying
     * tasks list.
     */
    public ArrayList<Task> occurringOn(TaskDateTime query) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.occursDuring(query)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
