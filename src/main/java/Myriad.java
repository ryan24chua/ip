import java.io.FileNotFoundException;
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
 * MyriadException, which is caught once per line in run() and shown as
 * an error.
 */
public class Myriad {

    /**
     * Recognized user commands. A line that isn't a recognized keyword
     * maps to UNKNOWN, which throws a MyriadException instead of doing
     * anything.
     */
    enum Command {
        UNKNOWN, LIST, EXIT, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Ui ui = new Ui();
        TaskList taskList = new TaskList();
        List<String> skippedLines = DataHandler.readData(taskList);

        ui.showGreeting();
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }
        run(ui, taskList);
        ui.showFarewell();
    }

    /**
     * Reads lines of user input until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input stream is exhausted, then returns so the caller can print
     * the farewell. Each line is dispatched by parseCommand: list/mark/
     * unmark/todo/deadline/event/delete run their respective handler, and
     * an unrecognized line throws MyriadException directly. Every handler may
     * throw MyriadException instead of displaying its own error, so this
     * is the single place that catches it and shows it — with the
     * "Error: " prefix added here rather than repeated in every message.
     * The add handlers also raise MyriadException (rather than a checked
     * IOException) when saving a task to disk fails, so a save failure is
     * caught and shown the same way as any other command error, instead of
     * crashing the program.
     */
    private static void run(Ui ui, TaskList taskList) {
        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().strip();
            Command cmd = parseCommand(line);

            try {
                switch (cmd) {
                    case EXIT -> {
                        return;
                    }
                    case LIST -> ui.showList(taskList.asList());
                    case UNKNOWN -> throw new MyriadException(
                            "I don't recognize that command. Try: todo, deadline, event, "
                                    + "list, mark, unmark, delete, or bye.");
                    case MARK -> markTask(line, taskList, ui);
                    case UNMARK -> unmarkTask(line, taskList, ui);
                    case TODO -> addToDo(line, taskList, ui);
                    case DEADLINE -> addDeadline(line, taskList, ui);
                    case EVENT -> addEvent(line, taskList, ui);
                    case DELETE -> deleteTask(line, taskList, ui);
                }
            } catch (MyriadException e) {
                ui.showError("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Maps a stripped input line to a Command. The first word is matched
     * case-insensitively against the known command keywords. "bye" and
     * "list" take no arguments, so they only match when they are the whole
     * line. "mark", "unmark", "todo", "deadline", "event", and "delete" all
     * match on keyword alone (their own handlers throw MyriadException if
     * the required argument text is missing). Anything else maps to
     * UNKNOWN.
     */
    private static Command parseCommand(String strippedLine) {
        String[] parts = strippedLine.split("\\s+", 2);
        String firstWord = parts[0];
        String rest = parts.length > 1 ? parts[1] : "";

        if (firstWord.equalsIgnoreCase("bye") && rest.isEmpty()) {
            return Command.EXIT;
        } else if (firstWord.equalsIgnoreCase("list") && rest.isEmpty()) {
            return Command.LIST;
        } else if (firstWord.equalsIgnoreCase("mark")) {
            return Command.MARK;
        } else if (firstWord.equalsIgnoreCase("unmark")) {
            return Command.UNMARK;
        } else if (firstWord.equalsIgnoreCase("todo")) {
            return Command.TODO;
        } else if (firstWord.equalsIgnoreCase("deadline")) {
            return Command.DEADLINE;
        } else if (firstWord.equalsIgnoreCase("event")) {
            return Command.EVENT;
        } else if (firstWord.equalsIgnoreCase("delete")) {
            return Command.DELETE;
        } else {
            return Command.UNKNOWN;
        }
    }

    /**
     * Parses the task number argument that follows a mark/unmark keyword
     * (e.g. the "2" in "mark 2") and returns its 0-based index into
     * taskList. Throws MyriadException if the argument is missing, not a
     * whole number, or out of range for the current list size — with a
     * message specific to which of those three went wrong.
     */
    private static int resolveTaskIndex(String line, TaskList taskList) throws MyriadException {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            throw new MyriadException(
                    "Please tell me which task number, e.g. \"mark 2\".");
        }

        String arg = parts[1].strip();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new MyriadException(
                    "\"" + arg + "\" is not a valid task number — it needs to be a whole "
                            + "number, e.g. \"mark 2\".");
        }

        int index = taskNumber - 1;
        if (!taskList.isValidIndex(index)) {
            if (taskList.size() == 0) {
                // Special-cased so the message doesn't say "choose a number
                // from 1 to 0", which the generic branch below would produce.
                throw new MyriadException(
                        "Task number " + taskNumber + " doesn't exist — your task list is "
                                + "empty. Add a task first with todo/deadline/event.");
            }
            throw new MyriadException(String.format(
                    "Task number %d doesn't exist. You have %d task(s) in the list, so "
                            + "please choose a number from 1 to %d.",
                    taskNumber, taskList.size(), taskList.size()));
        }
        return index;
    }

    /**
     * Saves taskList's current state to disk, wrapping any IOException
     * (disk full, permission denied, etc.) into a MyriadException so it's
     * shown by run()'s catch block like any other command error, instead
     * of crashing the program. Shared by every mutating command
     * (add/mark/unmark/delete) so there's one place that translates a save
     * failure into a user-facing message.
     */
    private static void save(TaskList taskList) throws MyriadException {
        try {
            DataHandler.saveAll(taskList);
        } catch (IOException e) {
            throw new MyriadException(
                    "Couldn't save your tasks to disk (the list is still correct for this "
                            + "session, but changes won't be there next time you start): " + e.getMessage());
        }
    }

    /**
     * Adds task to taskList, shows the standard added-task
     * acknowledgement, then saves. Shared by every add-command handler
     * (addToDo/addDeadline/addEvent) so the acknowledgement wording stays
     * consistent across task types. The add and acknowledgement happen
     * before the save is attempted, so a save failure never undoes the
     * in-memory add — the task still shows up in list for the rest of the
     * session even if it couldn't be written to disk.
     */
    private static void addAndShow(Task task, TaskList taskList, Ui ui) throws MyriadException {
        taskList.add(task);
        ui.showAddedTask(task, taskList.size());
        save(taskList);
    }

    /**
     * Marks the task named by the task number in line (e.g. "mark 2") as
     * done, shows an acknowledgement, then saves. Throws MyriadException
     * instead if the task number is missing, not a number, or out of
     * range.
     */
    private static void markTask(String line, TaskList taskList, Ui ui) throws MyriadException {
        int index = resolveTaskIndex(line, taskList);
        taskList.markDone(index);
        ui.showMarked(taskList.get(index));
        save(taskList);
    }

    /**
     * Marks the task named by the task number in line (e.g. "unmark 2") as
     * not done, shows an acknowledgement, then saves. Throws
     * MyriadException instead if the task number is missing, not a
     * number, or out of range.
     */
    private static void unmarkTask(String line, TaskList taskList, Ui ui) throws MyriadException {
        int index = resolveTaskIndex(line, taskList);
        taskList.markNotDone(index);
        ui.showUnmarked(taskList.get(index));
        save(taskList);
    }

    /**
     * Removes the task named by the task number in line (e.g. "delete 2"),
     * shows an acknowledgement, then saves. Throws MyriadException instead
     * if the task number is missing, not a number, or out of range.
     */
    private static void deleteTask(String line, TaskList taskList, Ui ui) throws MyriadException {
        int index = resolveTaskIndex(line, taskList);
        Task removed = taskList.remove(index);
        ui.showDeleted(removed, taskList.size());
        save(taskList);
    }

    /**
     * Parses a "todo <description>" line and, if a description is present,
     * adds a ToDo task and shows the standard added-task acknowledgement.
     * Throws MyriadException instead if the description is missing.
     */
    private static void addToDo(String line, TaskList taskList, Ui ui) throws MyriadException {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"todo read book\".");
        }
        addAndShow(new ToDo(parts[1]), taskList, ui);
    }

    /**
     * Splits text on the given marker (e.g. "/by"), matched
     * case-insensitively with optional surrounding whitespace consumed —
     * this mirrors the case-insensitive matching used for command keywords
     * elsewhere (e.g. "deadline" itself). Returns a 1-element array holding
     * all of the text if the marker isn't found, or a 2-element array of the
     * text before/after the marker if it is.
     */
    private static String[] splitOnMarker(String text, String marker) {
        return text.split("(?i)\\s*" + marker + "\\s*", 2);
    }

    /**
     * Parses a "deadline <description> /by <date>" line and, if both parts
     * are present, adds a Deadline task and shows the standard
     * added-task acknowledgement. Throws MyriadException instead if the
     * description or date is missing.
     */
    private static void addDeadline(String line, TaskList taskList, Ui ui) throws MyriadException {
        String[] parts = line.split("\\s+", 2);
        String argsText = parts.length == 2 ? parts[1] : "";

        String[] descAndDate = splitOnMarker(argsText, "/by");
        String description = descAndDate[0];
        String date = descAndDate.length == 2 ? descAndDate[1] : null;

        String example = "deadline return book /by 2019-12-02";
        if (description.isBlank()) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"" + example + "\".");
        } else if (date == null || date.isBlank()) {
            throw new MyriadException(
                    "Please include a date after /by, e.g. \"" + example + "\".");
        }
        addAndShow(new Deadline(description, TaskDateTime.parse(date)), taskList, ui);
    }

    /**
     * Parses an "event <description> /from <start> /to <end>" line and, if
     * all three parts are present, adds an Event task and shows the
     * standard added-task acknowledgement. Throws MyriadException instead
     * if the description, start date, or end date is missing.
     */
    private static void addEvent(String line, TaskList taskList, Ui ui) throws MyriadException {
        String[] parts = line.split("\\s+", 2);
        String argsText = parts.length == 2 ? parts[1] : "";

        String[] descAndRest = splitOnMarker(argsText, "/from");
        String description = descAndRest[0];
        String rest = descAndRest.length == 2 ? descAndRest[1] : "";

        String[] startAndEnd = splitOnMarker(rest, "/to");
        String start = startAndEnd[0];
        String end = startAndEnd.length == 2 ? startAndEnd[1] : null;

        String example = "event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600";
        if (description.isBlank()) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"" + example + "\".");
        } else if (start.isBlank()) {
            throw new MyriadException(
                    "Please include a start time after /from, e.g. \"" + example + "\".");
        } else if (end == null || end.isBlank()) {
            throw new MyriadException(
                    "Please include an end time after /to, e.g. \"" + example + "\".");
        }
        addAndShow(new Event(description, TaskDateTime.parse(start), TaskDateTime.parse(end)), taskList, ui);
    }
}
