import myriad.task.TaskDateTime;

/**
 * Shows every Deadline/Event occurring during a given date/time (see
 * TaskList.occurringOn and Task.occursDuring). Reads the list without
 * changing it, so it never saves.
 */
public class ShowCommand extends Command {

    private final TaskDateTime query;

    public ShowCommand(TaskDateTime query) {
        this.query = query;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOn(tasks.occurringOn(query), query);
    }
}
