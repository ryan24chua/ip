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
        ADD, LIST, EXIT;
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
        ArrayList<Task> textList = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().strip();
            Command cmd = parseCommand(line);

            switch (cmd) {
                case EXIT -> {
                    return;
                }
                case LIST -> printList(textList);
                case ADD -> {
                    textList.add(new Task(line));
                    acknowledgeAdd(line);
                }
            }
        }
    }

    /**
     * Maps a stripped input line to a Command. The whole line is matched
     * case-insensitively against the known command keywords; any line that
     * doesn't match one exactly is treated as ADD, with the whole line kept
     * as the task description.
     */
    private static Command parseCommand(String strippedLine) {
        return switch (strippedLine.toLowerCase()) {
            case "bye" -> Command.EXIT;
            case "list" -> Command.LIST;
            default -> Command.ADD;
        };
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
            System.out.printf("%d. %s%n", i + 1, lst.get(i));
        }
        printDivider();
    }
}