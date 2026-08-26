import myriad.MyriadException;

/**
 * Marks the task with the given number as not done yet.
 */
public class UnmarkCommand extends TaskNumberCommand {

    public UnmarkCommand(int taskNumber) {
        super(taskNumber);
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException {
        int index = resolveIndex(tasks);
        tasks.markNotDone(index);
        ui.showUnmarked(tasks.get(index));
        save(tasks, storage);
    }
}
