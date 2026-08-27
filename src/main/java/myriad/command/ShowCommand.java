package myriad.command;

import myriad.Storage;
import myriad.TaskList;
import myriad.Ui;
import myriad.task.TaskDateTime;

/**
 * Shows every Deadline/Event occurring during a given date/time (see
 * TaskList.occurringOn and Task.occursDuring). Reads the list without
 * changing it, so it never saves.
 */
public class ShowCommand extends Command {

    private final TaskDateTime query;

    /**
     * Creates a command that reports the tasks occurring during query.
     *
     * @param query the date, or date and time, to search for.
     */
    public ShowCommand(TaskDateTime query) {
        this.query = query;
    }

    /**
     * Shows the tasks occurring during the query date/time, or a "none
     * found" message if there are none.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOn(tasks.occurringOn(query), query);
    }
}
