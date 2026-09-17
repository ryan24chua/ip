package myriad.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Holds the user's tasks and the operations that mutate them (add,
 * mark done/not done). Has no console I/O of its own — Ui is solely
 * responsible for displaying anything about a TaskList's contents.
 */
public class TaskList {
    private final List<Task> tasks;

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
     *
     * @param initialTasks the tasks to start with; copied, not aliased.
     */
    public TaskList(List<Task> initialTasks) {
        assert initialTasks != null : "Storage.load always builds a list, even when empty";
        this.tasks = new ArrayList<>(initialTasks);
    }

    /**
     * Appends a task to the end of the list, so task numbers already shown
     * to the user keep referring to the same tasks.
     *
     * @param task the task to add.
     */
    public void add(Task task) {
        // A null would only fail later, in list or save, far from where it came from.
        assert task != null : "the Parser always builds a task before AddCommand runs";
        tasks.add(task);
    }

    /**
     * Returns the task at the given 0-based index.
     *
     * @param index a 0-based index; callers are expected to have checked it
     *              with isValidIndex first, since an out-of-range index is
     *              a programming bug: it fails an assertion (with -ea) or
     *              throws IndexOutOfBoundsException, never a
     *              MyriadException.
     * @return the task at that position.
     */
    public Task get(int index) {
        assert isValidIndex(index) : "index " + index + " should have been checked by resolveIndex";
        return tasks.get(index);
    }

    /**
     * Returns how many tasks the list holds.
     *
     * @return the number of tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list holds no tasks at all.
     *
     * @return true if there are no tasks.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns whether index is a valid 0-based index into this list.
     *
     * @param index the index to check.
     * @return true if a task currently sits at that index.
     */
    public boolean isValidIndex(int index) {
        return index >= 0 && index < tasks.size();
    }

    /**
     * Marks the task at the given 0-based index as done.
     *
     * @param index a 0-based index, expected to be valid (see get).
     */
    public void markDone(int index) {
        assert isValidIndex(index) : "index " + index + " should have been checked by resolveIndex";
        tasks.get(index).setDone(true);
    }

    /**
     * Marks the task at the given 0-based index as not done yet.
     *
     * @param index a 0-based index, expected to be valid (see get).
     */
    public void markNotDone(int index) {
        assert isValidIndex(index) : "index " + index + " should have been checked by resolveIndex";
        tasks.get(index).setDone(false);
    }

    /**
     * Removes and returns the task at the given 0-based index. Every later
     * task shifts down one, so the numbers the user sees change after a
     * delete — which is why task numbers are re-checked at execute time
     * rather than at parse time.
     *
     * @param index a 0-based index, expected to be valid (see get).
     * @return the task that was removed, so the caller can show it.
     */
    public Task remove(int index) {
        assert isValidIndex(index) : "index " + index + " should have been checked by resolveIndex";
        return tasks.remove(index);
    }

    /**
     * Returns a read-only view of every task, in task-number order. The view
     * is live, so it reflects later changes without being fetched again, but
     * any attempt to change it through the view throws
     * UnsupportedOperationException: tasks are only added, marked or removed
     * through this class's own methods.
     *
     * @return a read-only view of the tasks, in task-number order.
     */
    public List<Task> asList() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns a new list of every task whose occursDuring(query) is true,
     * for the "show task" command. Unlike asList(), this is a snapshot: it
     * does not change if the task list changes afterwards.
     *
     * @param query the date, or date and time, being asked about.
     * @return a new read-only list of the matching tasks, in their original order.
     */
    public List<Task> getTasksOccurringOn(TaskDateTime query) {
        return filter(task -> task.occursDuring(query));
    }

    /**
     * Returns a new list of every task whose description contains keyword,
     * ignoring case, for the "find" command. Like getTasksOccurringOn, this
     * is a read-only snapshot.
     *
     * @param keyword the text to look for in task descriptions.
     * @return a new read-only list of the matching tasks, in their original order.
     */
    public List<Task> getTasksMatching(String keyword) {
        return filter(task -> task.descriptionContains(keyword));
    }

    /**
     * Returns a new list of every task of the given type, for
     * the "stats" command's per-type breakdown. Like getTasksMatching, this
     * is a read-only snapshot; callers that just want the count can take
     * size() of the result.
     *
     * @param type the type to match, e.g. TaskType.DEADLINE.
     * @return a new read-only list of the matching tasks, in their original order.
     */
    public List<Task> getTasksOfType(TaskType type) {
        return filter(task -> task.getType() == type);
    }

    /**
     * Returns a new list of every task due within the next days days of
     * date, i.e. overlapping the period from the start of date to the end
     * of date.plusDays(days) — the same start/end convention
     * TaskDateTime.rangeStart()/rangeEnd() use for a whole day.
     *
     * @param date the date the period starts from (its start of day).
     * @param days how many days the period spans after date.
     * @return a new read-only list of the matching tasks, in their original order.
     */
    public List<Task> getTasksDueWithin(LocalDate date, int days) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(days).atTime(LocalTime.MAX);
        return filter(task -> task.overlaps(from, to));
    }

    /**
     * Returns a new list of the first limit undone tasks in list order, for
     * the "stats" command's "oldest not done" section. "Oldest" here means
     * earliest added, not an explicit timestamp — tasks are appended by
     * add() and never reordered, so list order already is add order.
     *
     * @param limit the maximum number of tasks to return; never negative.
     * @return a new read-only list of up to limit undone tasks, in their original order.
     */
    public List<Task> getOldestUndone(int limit) {
        // Stream.limit rejects a negative limit with its own exception; the
        // limit is a constant in code, so a negative one is a programming bug.
        assert limit >= 0 : "limit is a non-negative constant, e.g. StatsCommand.OLDEST_UNDONE_LIMIT";
        return tasks.stream()
                .filter(task -> !task.isDone())
                .limit(limit)
                .toList();
    }

    /**
     * Returns a new read-only list of every task that satisfies condition,
     * in task-number order. Shared by the query methods above, which differ
     * only in the condition they test.
     *
     * Uses a stream: filter keeps the tasks the condition accepts, and
     * toList collects them into a list that cannot be modified. A for loop
     * adding matches to an ArrayList, as these methods used to repeat, does
     * the same with more lines.
     *
     * @param condition the test a task must pass to be included.
     * @return a new read-only list of the matching tasks, in their original order.
     */
    private List<Task> filter(Predicate<Task> condition) {
        return tasks.stream()
                .filter(condition)
                .toList();
    }
}
