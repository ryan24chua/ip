/**
 * Entry point for the Myriad chatbot.
 * Greets the user, stores each line of input the user enters (acknowledging
 * each one as it's added) and lists them back on request, until the user
 * types the exit command, then prints a farewell.
 */
import java.util.Scanner;
import java.util.ArrayList;

public class Myriad {
    private static final String LINE = "____________________________________________________________";

    // Command that ends the input loop; matched case-insensitively.
    private static final String EXIT_COND = "bye";

    // Command to display the user text back.
    private static final String LIST_CMD = "list";

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
     * Reads lines of user input until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input stream is exhausted, then returns so the caller can print
     * the farewell. Each line is either treated as a command ({@code list})
     * or stored and acknowledged as an added item.
     */
    private static void run() {
        Scanner sc = new Scanner(System.in);

        // Store whatever text the user enters.
        ArrayList<String> textList = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // Exit Condition.
            if (line.strip().equalsIgnoreCase(EXIT_COND)) {
                break;
            }

            // Print list command.
            if (line.strip().equalsIgnoreCase(LIST_CMD)) {
                printList(textList);
                continue;
            }

            textList.add(line);
            acknowledgeAdd(line);
        }
    }

    /**
     * Prints an acknowledgement that the given line was added
     * (e.g. {@code added: read book}), framed by divider lines.
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
     * Prints every stored item as a 1-indexed numbered list, framed by
     * divider lines.
     */
    private static void printList(ArrayList<String> lst) {
        printDivider();
        for (int i = 0; i < lst.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, lst.get(i));
        }
        printDivider();
    }
}