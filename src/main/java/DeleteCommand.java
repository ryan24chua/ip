import myriad.MyriadException;
import myriad.task.Task;

/**
 * Removes the task with the given number from the list.
 */
public class DeleteCommand extends TaskNumberCommand {

    public DeleteCommand(int taskNumber) {
        super(taskNumber);
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException {
        int index = resolveIndex(tasks);
        Task removed = tasks.remove(index);
        ui.showDeleted(removed, tasks.size());
        save(tasks, storage);
    }
}
