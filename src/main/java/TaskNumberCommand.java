/**
 * Shared behaviour of the commands that name an existing task by its
 * number: mark, unmark and delete. All three take the same argument and
 * have to reject the same three ways of getting it wrong, so the number
 * and the check live here once instead of in each of them.
 */
public abstract class TaskNumberCommand extends Command {

    /** The task number as the user typed it: 1-based, not yet checked. */
    private final int taskNumber;

    protected TaskNumberCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Turns the task number into a 0-based index into tasks. Throws
     * MyriadException if no task has that number — the check happens here,
     * at execute time, rather than when the Parser built this command,
     * because whether a number is in range depends on how many tasks there
     * are at the moment the command runs.
     */
    protected int resolveIndex(TaskList tasks) throws MyriadException {
        int index = taskNumber - 1;
        if (!tasks.isValidIndex(index)) {
            if (tasks.size() == 0) {
                // Special-cased so the message doesn't say "choose a number
                // from 1 to 0", which the generic branch below would produce.
                throw new MyriadException(
                        "Task number " + taskNumber + " doesn't exist — your task list is "
                                + "empty. Add a task first with todo/deadline/event.");
            }
            throw new MyriadException(String.format(
                    "Task number %d doesn't exist. You have %d task(s) in the list, so "
                            + "please choose a number from 1 to %d.",
                    taskNumber, tasks.size(), tasks.size()));
        }
        return index;
    }
}
