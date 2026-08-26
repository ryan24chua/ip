/**
 * Shows every task in the list. Reads nothing but the list itself, so it
 * carries no arguments and never saves.
 */
public class ListCommand extends Command {

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks.asList());
    }
}
