/**
 * Entry point for the Myriad chatbot.
 * Currently only greets the user on startup and prints a farewell before terminating;
 * no user interaction is handled yet.
 */
import java.util.Scanner;

public class Myriad {
    private static final String LINE = "____________________________________________________________";

    public static void main(String[] args) {
        greet();
        echo();
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
     * Prints a farewell message followed by a divider line.
     * Note: does not call {@link System#exit}; the program simply returns from main
     * after this runs.
     */
    private static void exit() {
        String exitMsg = "Bye. Hope to see you again soon!";
        System.out.println(exitMsg);
        printDivider();
    }

    /**
     * Reads a single line of user input from standard input and echoes it
     * back, framed by divider lines.
     */
    private static void echo() {
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        printDivider();
        System.out.println(line);
        printDivider();
    }

    private static void printDivider() {
        System.out.println(LINE);
    }
}