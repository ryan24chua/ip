import myriad.MyriadException;

/**
 * Marks the task with the given number as done.
 */
public class MarkCommand extends TaskNumberCommand {

    public MarkCommand(int taskNumber) {
        super(taskNumber);
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException {
        int index = resolveIndex(tasks);
        tasks.markDone(index);
        ui.showMarked(tasks.get(index));
        save(tasks, storage);
    }
}
