import java.util.ArrayList;
import java.util.List;

/**
 * Owns every line printed to the console, including the divider framing
 * that used to be repeated at the top and bottom of nearly every handler
 * in Myriad. One show* method per user-facing interaction.
 */
public class Ui {
    private static final String LINE = "____________________________________________________________";

    private void showDivider() {
        System.out.println(LINE);
    }

    /**
     * Prints the startup banner and welcome message, framed by divider lines.
     */
    public void showGreeting() {
        String banner = "███╗   ███╗██╗   ██╗██████╗ ██╗ █████╗ ██████╗ \n"
                + "████╗ ████║╚██╗ ██╔╝██╔══██╗██║██╔══██╗██╔══██╗\n"
                + "██╔████╔██║ ╚████╔╝ ██████╔╝██║███████║██║  ██║\n"
                + "██║╚██╔╝██║  ╚██╔╝  ██╔══██╗██║██╔══██║██║  ██║\n"
                + "██║ ╚═╝ ██║   ██║   ██║  ██║██║██║  ██║██████╔╝\n"
                + "╚═╝     ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═════╝ ";
        String greeting = """
                Hello! I'm Myriad.
                What can I do for you?""";
        showDivider();
        System.out.println(banner);
        System.out.println(greeting);
        showDivider();
    }

    /**
     * Prints a farewell message, framed by divider lines.
     */
    public void showFarewell() {
        showDivider();
        System.out.println("Bye. Hope to see you again soon!");
        showDivider();
    }

    /**
     * Prints the standard added-task acknowledgement (the task's own
     * toString, plus the new list size), framed by divider lines.
     */
    public void showAddedTask(Task task, int totalCount) {
        showDivider();
        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.printf("Now you have %d tasks in the list.%n", totalCount);
        showDivider();
    }

    /**
     * Prints a header line followed by every task as a 1-indexed numbered
     * list, framed by divider lines.
     */
    public void showList(ArrayList<Task> tasks) {
        showDivider();
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d.%s%n", i + 1, tasks.get(i));
        }
        showDivider();
    }

    /**
     * Prints the tasks occurring during query (from the "show task"
     * command), 1-indexed like showList, or a "none found" message if
     * matches is empty. Framed by divider lines.
     */
    public void showTasksOn(ArrayList<Task> matches, TaskDateTime query) {
        showDivider();
        if (matches.isEmpty()) {
            System.out.printf("No deadlines or events found on %s.%n", query);
        } else {
            System.out.printf("Here are the tasks occurring on %s:%n", query);
            for (int i = 0; i < matches.size(); i++) {
                System.out.printf("%d.%s%n", i + 1, matches.get(i));
            }
        }
        showDivider();
    }

    /**
     * Prints an acknowledgement that task was marked done, framed by
     * divider lines.
     */
    public void showMarked(Task task) {
        showDivider();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + task);
        showDivider();
    }

    /**
     * Prints an acknowledgement that task was marked not done, framed by
     * divider lines.
     */
    public void showUnmarked(Task task) {
        showDivider();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + task);
        showDivider();
    }

    /**
     * Prints an acknowledgement that task was removed (the task's own
     * toString, plus the new list size), framed by divider lines.
     */
    public void showDeleted(Task task, int totalCount) {
        showDivider();
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + task);
        System.out.printf("Now you have %d tasks in the list.%n", totalCount);
        showDivider();
    }

    /**
     * Prints a single-line error/status message, framed by divider lines.
     */
    public void showError(String message) {
        showDivider();
        System.out.println(message);
        showDivider();
    }

    /**
     * Prints a consolidated warning that some lines in the saved data file
     * could not be loaded and were skipped, framed by divider lines. Each
     * entry in skippedLines is one already-formatted line description; the
     * other, valid tasks from the file are unaffected and already in the
     * task list by the time this is called.
     */
    public void showLoadWarning(List<String> skippedLines) {
        showDivider();
        System.out.printf("Warning: %d line(s) in your saved data could not be loaded and "
                + "were skipped:%n", skippedLines.size());
        for (String skipped : skippedLines) {
            System.out.println("  - " + skipped);
        }
        showDivider();
    }
}
