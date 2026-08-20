/**
 * Entry point for the Myriad chatbot.
 * Greets the user, then reads lines of input, each treated as a command:
 * add a task ("todo"/"deadline"/"event"), "list" the stored tasks,
 * "mark"/"unmark" a task done, until the user types the exit command
 * ("bye"), then prints a farewell. A line that doesn't match any known
 * command shows an unknown-command error.
 */

import java.util.Scanner;

public class Myriad {

    /**
     * Recognized user commands. A line that isn't a recognized keyword
     * maps to UNKNOWN, which shows an error instead of doing anything.
     */
    enum Command {
        UNKNOWN, LIST, EXIT, MARK, UNMARK, TODO, DEADLINE, EVENT;
    }

    public static void main(String[] args) {
        Ui ui = new Ui();
        TaskList taskList = new TaskList();

        ui.showGreeting();
        run(ui, taskList);
        ui.showFarewell();
    }

    /**
     * Reads lines of user input until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input stream is exhausted, then returns so the caller can print
     * the farewell. Each line is dispatched by parseCommand: list/mark/
     * unmark/todo/deadline/event run their respective handler, and any
     * unrecognized line shows an unknown-command error.
     */
    private static void run(Ui ui, TaskList taskList) {
        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().strip();
            Command cmd = parseCommand(line);

            switch (cmd) {
                case EXIT -> {
                    return;
                }
                case LIST -> ui.showList(taskList.asList());
                case UNKNOWN -> showUnknownCommand(ui);
                case MARK -> markTask(line, taskList, ui);
                case UNMARK -> unmarkTask(line, taskList, ui);
                case TODO -> addToDo(line, taskList, ui);
                case DEADLINE -> addDeadline(line, taskList, ui);
                case EVENT -> addEvent(line, taskList, ui);
            }
        }
    }

    /**
     * Maps a stripped input line to a Command. The first word is matched
     * case-insensitively against the known command keywords. "bye" and
     * "list" take no arguments, so they only match when they are the whole
     * line. "mark", "unmark", "todo", "deadline", and "event" all match on
     * keyword alone (their own handlers report an error if the required
     * argument text is missing). Anything else maps to UNKNOWN.
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
        } else {
            return Command.UNKNOWN;
        }
    }

    /**
     * Parses the task number argument that follows a mark/unmark keyword
     * (e.g. the "2" in "mark 2"), returning its 0-based index into
     * taskList, or -1 if the argument is missing, not a number, or out of
     * range.
     */
    private static int parseTaskIndex(String line, TaskList taskList) {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            return -1;
        }

        try {
            int index = Integer.parseInt(parts[1].strip()) - 1;
            return taskList.isValidIndex(index) ? index : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Explains why parseTaskIndex(line, taskList) returned -1 for this
     * line: the task number argument is missing, not a whole number, or
     * out of range for the current list size.
     */
    private static String taskIndexErrorMessage(String line, TaskList taskList) {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            return "OOPS!!! Please tell me the task number, e.g. \"mark 2\".";
        }

        String arg = parts[1].strip();
        try {
            int taskNumber = Integer.parseInt(arg);
            return String.format(
                    "OOPS!!! Task number %d doesn't exist. You have %d task(s) in the list.",
                    taskNumber, taskList.size());
        } catch (NumberFormatException e) {
            return "OOPS!!! \"" + arg + "\" is not a valid task number.";
        }
    }

    /**
     * Adds task to taskList and shows the standard added-task
     * acknowledgement. Shared by every add-command handler (addToDo/
     * addDeadline/addEvent) so the acknowledgement wording stays
     * consistent across task types.
     */
    private static void addAndShow(Task task, TaskList taskList, Ui ui) {
        taskList.add(task);
        ui.showAddedTask(task, taskList.size());
    }

    /**
     * Shows the standard error for a line that didn't match any known
     * command.
     */
    private static void showUnknownCommand(Ui ui) {
        ui.showError("OOPS!!! I'm sorry, but I don't know what that means :-(");
    }

    /**
     * Marks the task named by the task number in line (e.g. "mark 2") as
     * done and shows an acknowledgement. Shows an error instead if the
     * task number is missing, not a number, or out of range.
     */
    private static void markTask(String line, TaskList taskList, Ui ui) {
        int index = parseTaskIndex(line, taskList);

        if (index == -1) {
            ui.showError(taskIndexErrorMessage(line, taskList));
        } else {
            taskList.markDone(index);
            ui.showMarked(taskList.get(index));
        }
    }

    /**
     * Marks the task named by the task number in line (e.g. "unmark 2") as
     * not done and shows an acknowledgement. Shows an error instead if
     * the task number is missing, not a number, or out of range.
     */
    private static void unmarkTask(String line, TaskList taskList, Ui ui) {
        int index = parseTaskIndex(line, taskList);

        if (index == -1) {
            ui.showError(taskIndexErrorMessage(line, taskList));
        } else {
            taskList.markNotDone(index);
            ui.showUnmarked(taskList.get(index));
        }
    }

    /**
     * Parses a "todo <description>" line and, if a description is present,
     * adds a ToDo task and shows the standard added-task acknowledgement.
     * Shows an error instead if the description is missing.
     */
    private static void addToDo(String line, TaskList taskList, Ui ui) {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            ui.showError("OOPS!!! Please include task description.");
        } else {
            addAndShow(new ToDo(parts[1]), taskList, ui);
        }
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
     * added-task acknowledgement. Shows an error instead if the
     * description or date is missing.
     */
    private static void addDeadline(String line, TaskList taskList, Ui ui) {
        String[] parts = line.split("\\s+", 2);
        String argsText = parts.length == 2 ? parts[1] : "";

        String[] descAndDate = splitOnMarker(argsText, "/by");
        String description = descAndDate[0];
        String date = descAndDate.length == 2 ? descAndDate[1] : null;

        if (description.isBlank()) {
            ui.showError("OOPS!!! Please include task description.");
        } else if (date == null || date.isBlank()) {
            ui.showError("OOPS!!! Please include date.");
        } else {
            addAndShow(new Deadline(description, date), taskList, ui);
        }
    }

    /**
     * Parses an "event <description> /from <start> /to <end>" line and, if
     * all three parts are present, adds an Event task and shows the
     * standard added-task acknowledgement. Shows an error instead if the
     * description, start date, or end date is missing.
     */
    private static void addEvent(String line, TaskList taskList, Ui ui) {
        String[] parts = line.split("\\s+", 2);
        String argsText = parts.length == 2 ? parts[1] : "";

        String[] descAndRest = splitOnMarker(argsText, "/from");
        String description = descAndRest[0];
        String rest = descAndRest.length == 2 ? descAndRest[1] : "";

        String[] startAndEnd = splitOnMarker(rest, "/to");
        String start = startAndEnd[0];
        String end = startAndEnd.length == 2 ? startAndEnd[1] : null;

        if (description.isBlank()) {
            ui.showError("OOPS!!! Please include task description.");
        } else if (start.isBlank()) {
            ui.showError("OOPS!!! Please include start date.");
        } else if (end == null || end.isBlank()) {
            ui.showError("OOPS!!! Please include end date.");
        } else {
            addAndShow(new Event(description, start, end), taskList, ui);
        }
    }
}
