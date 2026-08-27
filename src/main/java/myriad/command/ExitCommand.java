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

    /** Creates the command "bye" asks for; it takes no arguments. */
    public ExitCommand() {
    }

    /**
     * Does nothing: this command's whole effect is the isExit() below.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: isExit() below is what stops the command loop.
    }

    /**
     * Returns true, which is what stops Myriad's command loop.
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
