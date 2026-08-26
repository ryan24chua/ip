import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Myriad chatbot.
 * Loads any previously saved tasks from disk, greets the user, then reads
 * lines of input, each treated as a command:
 * add a task ("todo"/"deadline"/"event"), "list" the stored tasks,
 * "mark"/"unmark" a task done, until the user types the exit command
 * ("bye"), then prints a farewell. A line that doesn't match any known
 * command, or that's missing a required argument, throws a
 * MyriadException, which is caught once per line in readCommands() and
 * shown as an error.
 *
 * One chatbot session is one Myriad object: it holds the pieces that
 * session needs — the Ui it talks through, the Storage it saves to, and
 * the TaskList it works on — as fields, so the command handlers can use
 * them without every one of them taking all three as parameters.
 */
public class Myriad {

    private final Ui ui;
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Descriptions of any saved-data lines that couldn't be loaded, or null
     * if the whole file couldn't be read. Both are held from construction
     * until run() shows them, because a load problem has to be reported
     * after the greeting rather than before it.
     */
    private final List<String> skippedLines;
    private final String loadErrorMessage;

    /**
     * Sets up one chatbot session: creates the Ui, points Storage at
     * filePath, and loads whatever tasks were saved there last session. A
     * file that can't be read at all isn't fatal — the session starts from
     * an empty list and run() warns about it — so that a single unreadable
     * file doesn't stop the user from using the chatbot at all.
     */
    public Myriad(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);

        TaskList loadedTasks;
        List<String> loadedSkippedLines;
        String errorMessage;
        try {
            LoadResult loaded = storage.load();
            loadedTasks = new TaskList(loaded.tasks());
            loadedSkippedLines = loaded.skippedLines();
            errorMessage = null;
        } catch (MyriadException e) {
            loadedTasks = new TaskList();
            loadedSkippedLines = List.of();
            errorMessage = e.getMessage();
        }
        this.tasks = loadedTasks;
        this.skippedLines = loadedSkippedLines;
        this.loadErrorMessage = errorMessage;
    }

    /**
     * Greets the user, warns about anything that couldn't be loaded,
     * handles commands until the user exits, then says goodbye.
     */
    public void run() {
        ui.showGreeting();
        if (loadErrorMessage != null) {
            ui.showLoadingError(loadErrorMessage);
        }
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }
        readCommands();
        ui.showFarewell();
    }

    public static void main(String[] args) {
        // Built with File rather than a "data/myriad.txt" literal so the
        // separator is right on every OS.
        new Myriad(new File("data", "myriad.txt").getPath()).run();
    }

    /**
     * Reads lines of user input until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input stream is exhausted, then returns so the caller can print
     * the farewell. Each line is dispatched on the CommandType the Parser
     * reports: list/mark/unmark/todo/deadline/event/delete run their
     * respective handler, given only that line's argument text (also from
     * the Parser), and an unrecognized line throws MyriadException
     * directly. Every handler may
     * throw MyriadException instead of displaying its own error, so this
     * is the single place that catches it and shows it — with the
     * "Error: " prefix added here rather than repeated in every message.
     * The add handlers also raise MyriadException (rather than a checked
     * IOException) when saving a task to disk fails, so a save failure is
     * caught and shown the same way as any other command error, instead of
     * crashing the program.
     */
    private void readCommands() {
        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().strip();
            CommandType cmd = Parser.parseCommandType(line);
            String args = Parser.extractArguments(line);

            try {
                switch (cmd) {
                    case EXIT -> {
                        return;
                    }
                    case LIST -> ui.showList(tasks.asList());
                    case UNKNOWN -> throw new MyriadException(
                            "I don't recognize that command. Try: todo, deadline, event, "
                                    + "list, mark, unmark, delete, show, or bye.");
                    case MARK -> markTask(args);
                    case UNMARK -> unmarkTask(args);
                    case TODO -> addToDo(args);
                    case DEADLINE -> addDeadline(args);
                    case EVENT -> addEvent(args);
                    case DELETE -> deleteTask(args);
                    case SHOW -> showTasksOn(args);
                }
            } catch (MyriadException e) {
                ui.showError("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Turns the task number argument of a mark/unmark/delete command into
     * a 0-based index into the task list. The Parser decides whether the
     * argument is a whole number at all; the range check stays here
     * because only this object knows how many tasks there currently are.
     * Throws MyriadException if the number doesn't name an existing task,
     * with a message specific to which case it is.
     */
    private int resolveTaskIndex(String args) throws MyriadException {
        int taskNumber = Parser.parseTaskNumber(args);

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

    /**
     * Saves the task list's current state to disk, wrapping any IOException
     * (disk full, permission denied, etc.) into a MyriadException so it's
     * shown by readCommands()'s catch block like any other command error,
     * instead of crashing the program. Shared by every mutating command
     * (add/mark/unmark/delete) so there's one place that translates a save
     * failure into a user-facing message.
     */
    private void save() throws MyriadException {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            throw new MyriadException(
                    "Couldn't save your tasks to disk (the list is still correct for this "
                            + "session, but changes won't be there next time you start): " + e.getMessage());
        }
    }

    /**
     * Adds task to the task list, shows the standard added-task
     * acknowledgement, then saves. Shared by every add-command handler
     * (addToDo/addDeadline/addEvent) so the acknowledgement wording stays
     * consistent across task types. The add and acknowledgement happen
     * before the save is attempted, so a save failure never undoes the
     * in-memory add — the task still shows up in list for the rest of the
     * session even if it couldn't be written to disk.
     */
    private void addAndShow(Task task) throws MyriadException {
        tasks.add(task);
        ui.showAddedTask(task, tasks.size());
        save();
    }

    /**
     * Marks the task named by args (e.g. the "2" of "mark 2") as done,
     * shows an acknowledgement, then saves. Throws MyriadException
     * instead if the task number is missing, not a number, or out of
     * range.
     */
    private void markTask(String args) throws MyriadException {
        int index = resolveTaskIndex(args);
        tasks.markDone(index);
        ui.showMarked(tasks.get(index));
        save();
    }

    /**
     * Marks the task named by args (e.g. the "2" of "unmark 2") as not
     * done, shows an acknowledgement, then saves. Throws
     * MyriadException instead if the task number is missing, not a
     * number, or out of range.
     */
    private void unmarkTask(String args) throws MyriadException {
        int index = resolveTaskIndex(args);
        tasks.markNotDone(index);
        ui.showUnmarked(tasks.get(index));
        save();
    }

    /**
     * Removes the task named by args (e.g. the "2" of "delete 2"), shows
     * an acknowledgement, then saves. Throws MyriadException instead if
     * the task number is missing, not a number, or out of range.
     */
    private void deleteTask(String args) throws MyriadException {
        int index = resolveTaskIndex(args);
        Task removed = tasks.remove(index);
        ui.showDeleted(removed, tasks.size());
        save();
    }

    /**
     * Adds the ToDo described by a "todo" command's arguments, and shows
     * the standard added-task acknowledgement. Throws MyriadException
     * instead if the Parser can't make a ToDo out of them.
     */
    private void addToDo(String args) throws MyriadException {
        addAndShow(Parser.parseToDo(args));
    }

    /**
     * Adds the Deadline described by a "deadline" command's arguments, and
     * shows the standard added-task acknowledgement. Throws
     * MyriadException instead if the Parser can't make a Deadline out of
     * them.
     */
    private void addDeadline(String args) throws MyriadException {
        addAndShow(Parser.parseDeadline(args));
    }

    /**
     * Adds the Event described by an "event" command's arguments, and
     * shows the standard added-task acknowledgement. Throws
     * MyriadException instead if the Parser can't make an Event out of
     * them.
     */
    private void addEvent(String args) throws MyriadException {
        addAndShow(Parser.parseEvent(args));
    }

    /**
     * Shows every Deadline/Event occurring during the date/time a "show"
     * command asks about (see TaskList.occurringOn and
     * Task.occursDuring). Throws MyriadException if that date/time is
     * missing or doesn't parse.
     */
    private void showTasksOn(String args) throws MyriadException {
        TaskDateTime query = Parser.parseShowQuery(args);
        var matches = tasks.occurringOn(query);
        ui.showTasksOn(matches, query);
    }
}
