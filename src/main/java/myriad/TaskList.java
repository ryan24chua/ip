package myriad;

import myriad.task.Task;
import myriad.task.TaskDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the user's tasks and the operations that mutate them (add,
 * mark done/not done). Has no console I/O of its own — Ui is solely
 * responsible for displaying anything about a TaskList's contents.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /**
     * Creates an empty task list, for a first run with nothing saved yet.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding the given tasks, e.g. the ones just
     * loaded from disk. The tasks are copied into a list of this object's
     * own rather than kept as an alias of initialTasks, so a later change
     * to the caller's list can't quietly change this TaskList behind the
     * back of its own add/remove methods.
     */
    public TaskList(List<Task> initialTasks) {
        this.tasks = new ArrayList<>(initialTasks);
    }

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

    /**
     * Returns a new list of every task whose description contains keyword,
     * ignoring case, for the "find" command. Like getTasksOccurringOn, this
     * is always a fresh list rather than the backing one.
     *
     * @param keyword the text to look for in task descriptions.
     * @return a fresh list of the matching tasks, in their original order.
     */
    public ArrayList<Task> getTasksMatching(String keyword) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.descriptionContains(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
