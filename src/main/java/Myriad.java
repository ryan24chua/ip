/**
 * Entry point for the Myriad chatbot.
 * Greets the user, echoes each line of input back until the user types the
 * exit command, then prints a farewell.
 */
import java.util.Scanner;

public class Myriad {
    private static final String LINE = "____________________________________________________________";

    // Command that ends the input loop; matched case-insensitively.
    private static final String EXIT_COND = "bye";

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
     * Note: does not call {@link System#exit}; the program simply returns from main
     * after this runs.
     */
    private static void exit() {
        printDivider();
        String exitMsg = "Bye. Hope to see you again soon!";
        System.out.println(exitMsg);
        printDivider();
    }

    /**
     * Reads lines of user input and echoes each one back until the user
     * enters the exit command (matched case-insensitively, ignoring
     * leading/trailing whitespace) or the input stream is exhausted, then
     * returns so the caller can print the farewell.
     */
    private static void run() {
        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.strip().equalsIgnoreCase(EXIT_COND)) {
                break;
            }
            echo(line);
        }
    }

    /**
     * Reads a single line of user input from standard input and echoes it
     * back, framed by divider lines.
     */
    private static void echo(String line) {
        printDivider();
        System.out.println(line);
        printDivider();
    }

    /**
     * Prints a divider line to visually separate sections of output.
     */
    private static void printDivider() {
        System.out.println(LINE);
    }
}