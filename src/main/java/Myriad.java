/**
 * Entry point for the Myriad chatbot.
 * Greets the user, stores each line of input the user enters (acknowledging
 * each one as it's added) and lists them back on request, until the user
 * types the exit command, then prints a farewell.
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
        ADD, LIST, EXIT, MARK, UNMARK;
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
     * the farewell. Each line is either treated as a command (e.g. list)
     * or stored and acknowledged as an added item.
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
                case ADD -> {
                    taskList.add(new Task(line));
                    acknowledgeAdd(line);
                }
                case MARK -> markTask(line, taskList);
                case UNMARK -> unmarkTask(line, taskList);
            }
        }
    }

    /**
     * Maps a stripped input line to a Command. The first word is matched
     * case-insensitively against the known command keywords. "bye" and
     * "list" take no arguments, so they only match when they are the whole
     * line; "mark" and "unmark" require a trailing argument (the task
     * number). Anything else is treated as ADD, with the whole line kept as
     * the task description.
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
     * Prints an acknowledgement that the given line was added
     * (e.g. added: read book), framed by divider lines.
     */
    private static void acknowledgeAdd(String line) {
        printDivider();
        System.out.println("added: " + line);
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
}