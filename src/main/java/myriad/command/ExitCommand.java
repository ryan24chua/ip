package myriad.command;

import myriad.Storage;
import myriad.TaskList;
import myriad.Ui;

/**
 * Ends the session. Does nothing when executed — the farewell is printed
 * by Myriad.run() once the command loop has stopped, so that it happens
 * exactly once whether the user typed "bye" or the input simply ran out.
 */
public class ExitCommand extends Command {

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: isExit() below is what stops the command loop.
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
