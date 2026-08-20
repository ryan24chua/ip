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

    public ArrayList<Task> asList() {
        return tasks;
    }
}
