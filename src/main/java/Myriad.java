/**
 * Entry point for the Myriad chatbot.
 * Greets the user, then reads lines of input, each treated as a command:
 * add a task ("todo"/"deadline"/"event", or a plain line for a raw task),
 * "list" the stored tasks, "mark"/"unmark" a task done, until the user
 * types the exit command ("bye"), then prints a farewell.
 */

import java.util.ArrayList;
import java.util.Scanner;

public class Myriad {
    private static final String LINE = "____________________________________________________________";

    /**
     * Recognized user commands. A line that isn't a recognized keyword is
     * treated as ADD (i.e. the whole line is a task to store).
     */
    enum Command {
        ADD, LIST, EXIT, MARK, UNMARK, TODO, DEADLINE, EVENT;
    }

    public static void main(String[] args) {
        greet();
        run();
        exit();
    }

    /**
     * Prints the startup banner and welcome message, framed by divider lines.
     */
    private static void greet() {
        String banner = "███╗   ███╗██╗   ██╗██████╗ ██╗ █████╗ ██████╗ \n"
                + "████╗ ████║╚██╗ ██╔╝██╔══██╗██║██╔══██╗██╔══██╗\n"
                + "██╔████╔██║ ╚████╔╝ ██████╔╝██║███████║██║  ██║\n"
                + "██║╚██╔╝██║  ╚██╔╝  ██╔══██╗██║██╔══██║██║  ██║\n"
                + "██║ ╚═╝ ██║   ██║   ██║  ██║██║██║  ██║██████╔╝\n"
                + "╚═╝     ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═════╝ ";
        String greeting = """
                Hello! I'm Myriad.
                What can I do for you?""";
        printDivider();
        System.out.println(banner);
        System.out.println(greeting);
        printDivider();
    }

    /**
     * Prints a farewell message, framed by divider lines.
     * Note: does not call System.exit; the program simply returns from main
     * after this runs.
     */
    private static void exit() {
        printDivider();
        String exitMsg = "Bye. Hope to see you again soon!";
        System.out.println(exitMsg);
        printDivider();
    }

    /**
     * Reads lines of user input until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input stream is exhausted, then returns so the caller can print
     * the farewell. Each line is dispatched by parseCommand: list/mark/
     * unmark/todo/deadline/event run their respective handler, and any
     * other line is stored and acknowledged as a raw added task.
     */
    private static void run() {
        Scanner sc = new Scanner(System.in);

        // Store whatever text the user enters.
        ArrayList<Task> taskList = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().strip();
            Command cmd = parseCommand(line);

            switch (cmd) {
                case EXIT -> {
                    return;
                }
                case LIST -> printList(taskList);
                case ADD -> addDefault(line, taskList);
                case MARK -> markTask(line, taskList);
                case UNMARK -> unmarkTask(line, taskList);
                case TODO -> addToDo(line, taskList);
                case DEADLINE -> addDeadline(line, taskList);
                case EVENT -> addEvent(line, taskList);
            }
        }
    }

    /**
     * Maps a stripped input line to a Command. The first word is matched
     * case-insensitively against the known command keywords. "bye" and
     * "list" take no arguments, so they only match when they are the whole
     * line; "mark" and "unmark" require a trailing argument (the task
     * number). "todo", "deadline", and "event" match on keyword alone
     * (their own handlers report an error if the required argument text is
     * missing, rather than falling back to ADD). Anything else is treated
     * as ADD, with the whole line kept as the task description.
     */
    private static Command parseCommand(String strippedLine) {
        String[] parts = strippedLine.split("\\s+", 2);
        String firstWord = parts[0];
        String rest = parts.length > 1 ? parts[1] : "";

        if (firstWord.equalsIgnoreCase("bye") && rest.isEmpty()) {
            return Command.EXIT;
        } else if (firstWord.equalsIgnoreCase("list") && rest.isEmpty()) {
            return Command.LIST;
        } else if (firstWord.equalsIgnoreCase("mark") && !rest.isEmpty()) {
            return Command.MARK;
        } else if (firstWord.equalsIgnoreCase("unmark") && !rest.isEmpty()) {
            return Command.UNMARK;
        } else if (firstWord.equalsIgnoreCase("todo")) {
            return Command.TODO;
        } else if (firstWord.equalsIgnoreCase("deadline")) {
            return Command.DEADLINE;
        } else if (firstWord.equalsIgnoreCase("event")) {
            return Command.EVENT;
        } else {
            return Command.ADD;
        }
    }

    /**
     * Parses the task number argument that follows a mark/unmark keyword
     * (e.g. the "2" in "mark 2"), returning its 0-based index into
     * taskList, or -1 if the argument is missing, not a number, or out of
     * range.
     */
    private static int parseTaskIndex(String line, ArrayList<Task> taskList) {
        String[] parts = line.split("\\s+", 2);

        if (parts.length < 2) {
            return -1;
        }

        try {
            int index = Integer.parseInt(parts[1].strip()) - 1;
            return (index >= 0 && index < taskList.size()) ? index : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Explains why parseTaskIndex(line, taskList) returned -1 for this
     * line: the task number argument is missing, not a whole number, or
     * out of range for the current list size.
     */
    private static String taskIndexErrorMessage(String line, ArrayList<Task> taskList) {
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
     * Wraps the whole (unrecognized-keyword) line as a raw Task, adds it,
     * and prints the standard added-task acknowledgement, framed by
     * divider lines.
     */
    private static void addDefault(String line, ArrayList<Task> taskList) {
        Task task = new Task(line);

        printDivider();
        addTask(task, taskList);
        printDivider();
    }

    /**
     * Prints a divider line to visually separate sections of output.
     */
    private static void printDivider() {
        System.out.println(LINE);
    }

    /**
     * Prints a header line followed by every stored item as a 1-indexed
     * numbered list, framed by divider lines.
     */
    private static void printList(ArrayList<Task> lst) {
        printDivider();
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < lst.size(); i++) {
            System.out.printf("%d.%s%n", i + 1, lst.get(i));
        }
        printDivider();
    }

    /**
     * Marks the task named by the task number in line (e.g. "mark 2") as
     * done and prints an acknowledgement. Prints an error instead if the
     * task number is missing, not a number, or out of range.
     */
    private static void markTask(String line, ArrayList<Task> taskList) {
        int index = parseTaskIndex(line, taskList);

        printDivider();

        if (index == -1) {
            System.out.println(taskIndexErrorMessage(line, taskList));
        } else {
            Task task = taskList.get(index);
            task.setDone(true);
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + task);
        }

        printDivider();
    }

    /**
     * Marks the task named by the task number in line (e.g. "unmark 2") as
     * not done and prints an acknowledgement. Prints an error instead if
     * the task number is missing, not a number, or out of range.
     */
    private static void unmarkTask(String line, ArrayList<Task> taskList) {
        int index = parseTaskIndex(line, taskList);

        printDivider();

        if (index == -1) {
            System.out.println(taskIndexErrorMessage(line, taskList));
        } else {
            Task task = taskList.get(index);
            task.setDone(false);
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  " + task);
        }

        printDivider();
    }

    /**
     * Appends task to taskList and prints the standard added-task
     * acknowledgement (task's own toString, plus the new list size). Shared
     * by every add-command handler (addDefault/addToDo/addDeadline/
     * addEvent) so the acknowledgement wording stays consistent across
     * task types. Does not print divider lines; callers frame the call
     * with printDivider() themselves since they may print an error instead
     * of calling this at all.
     */
    private static void addTask(Task task, ArrayList<Task> taskList) {
        taskList.add(task);

        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.printf("Now you have %d tasks in the list.%n", taskList.size());
    }

    /**
     * Parses a "todo <description>" line and, if a description is present,
     * adds a ToDo task and prints the standard added-task acknowledgement.
     * Prints an error instead if the description is missing.
     */
    private static void addToDo(String line, ArrayList<Task> taskList) {
        String[] parts = line.split("\\s+", 2);

        printDivider();

        if (parts.length < 2) {
            System.out.println("OOPS!!! Please include task description.");
        } else {
            ToDo task = new ToDo(parts[1]);
            addTask(task, taskList);
        }

        printDivider();
    }

    /**
     * Parses a "deadline <description> /by <date>" line and, if both parts
     * are present, adds a Deadline task and prints the standard
     * added-task acknowledgement. Prints an error instead if the
     * description or date is missing.
     */
    private static void addDeadline(String line, ArrayList<Task> taskList) {
        String[] parts = line.split("\\s+", 2);

        printDivider();

        String description = null;
        String date = null;

        if (parts.length == 2) {
            String text = parts[1];
            // Split case-insensitively so "/by", "/BY", "/By" etc. all
            // work, matching the case-insensitive matching used for
            // command keywords elsewhere (e.g. "deadline" itself).
            String[] descAndDate = text.split("(?i)\\s*/by\\s*", 2);

            description = descAndDate[0];
            if (descAndDate.length == 2) {
                // A match was found, so a date follows "/by".
                date = descAndDate[1];
            }
        }

        if (description == null || description.isBlank()) {
            System.out.println("OOPS!!! Please include task description.");
        } else if (date == null || date.isBlank()) {
            System.out.println("OOPS!!! Please include date.");
        } else {
            Deadline task = new Deadline(description, date);
            addTask(task, taskList);
        }

        printDivider();
    }

    // TODO: not yet implemented — should parse "event <description>
    // /from <start> /to <end>" the way addDeadline parses "/by", then add
    // an Event task via addTask.
    private static void addEvent(String line, ArrayList<Task> taskList) {

    }
}