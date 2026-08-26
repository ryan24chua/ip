import myriad.MyriadException;
import myriad.task.Task;

/**
 * Adds a task to the list. One class serves "todo", "deadline" and
 * "event" alike: ToDo, Deadline and Event already differ from each other
 * as Task subclasses, so what to add is decided by the Parser when it
 * builds the task, and three near-identical add commands would only
 * duplicate that distinction here.
 */
public class AddCommand extends Command {

    private final Task task;

    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Adds the task, shows the standard added-task acknowledgement, then
     * saves. The add and acknowledgement happen before the save is
     * attempted, so a save failure never undoes the in-memory add — the
     * task still shows up in list for the rest of the session even if it
     * couldn't be written to disk.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException {
        tasks.add(task);
        ui.showAddedTask(task, tasks.size());
        save(tasks, storage);
    }
}
