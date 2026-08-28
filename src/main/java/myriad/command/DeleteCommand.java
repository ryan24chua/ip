package myriad.command;

import myriad.MyriadException;
import myriad.Storage;
import myriad.TaskList;
import myriad.Ui;
import myriad.task.Task;

/**
 * Removes the task with the given number from the list.
 */
public class DeleteCommand extends TaskNumberCommand {

    /**
     * Creates a command that deletes the task the user numbered.
     *
     * @param taskNumber the task number as typed, 1-based and not yet
     *                   checked against the list (see
     *                   TaskNumberCommand.resolveIndex).
     */
    public DeleteCommand(int taskNumber) {
        super(taskNumber);
    }

    /**
     * Removes the numbered task, shows what was removed along with the new
     * list size, then saves.
     *
     * @throws MyriadException if no task has that number, or if saving fails.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException {
        int index = resolveIndex(tasks);
        Task removed = tasks.remove(index);
        ui.showDeleted(removed, tasks.size());
        save(tasks, storage);
    }
}
