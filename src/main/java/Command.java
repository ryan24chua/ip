import java.io.IOException;

/**
 * One command the user asked for, ready to be carried out. The Parser
 * turns a line of input into the matching Command subclass, and
 * Myriad.readCommands() just calls execute() on whatever it gets back —
 * so the loop doesn't need to know the list of commands at all, and
 * adding a command means adding a subclass here rather than editing a
 * switch in Myriad.
 *
 * Each subclass holds whatever its own arguments were (a Task to add, a
 * task number, a date to search for), so by the time execute() runs there
 * is no input text left to interpret.
 */
public abstract class Command {

    /**
     * Carries out this command against the given task list, reporting
     * whatever the user should see through ui and persisting any change
     * through storage. Throws MyriadException if the command can't be
     * carried out (e.g. it names a task that doesn't exist); readCommands()
     * catches that and shows it as an error.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws MyriadException;

    /**
     * Whether the session should end after this command. False for every
     * command except ExitCommand, which is why the default is here rather
     * than repeated in each subclass.
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Saves the task list's current state, wrapping any IOException (disk
     * full, permission denied, etc.) into a MyriadException so it's shown
     * like any other command error instead of crashing the program.
     * Provided here, rather than in each subclass, so every mutating
     * command (add/mark/unmark/delete) translates a save failure into the
     * same user-facing message.
     */
    protected void save(TaskList tasks, Storage storage) throws MyriadException {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            throw new MyriadException(
                    "Couldn't save your tasks to disk (the list is still correct for this "
                            + "session, but changes won't be there next time you start): " + e.getMessage());
        }
    }
}
