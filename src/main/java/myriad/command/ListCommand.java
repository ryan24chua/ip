package myriad.command;

import myriad.Storage;
import myriad.TaskList;
import myriad.Ui;

/**
 * Shows every task in the list. Reads nothing but the list itself, so it
 * carries no arguments and never saves.
 */
public class ListCommand extends Command {

    /** Creates the command "list" asks for; it takes no arguments. */
    public ListCommand() {
    }

    /**
     * Shows every task in the list, numbered, through the Ui.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks.asList());
    }
}
