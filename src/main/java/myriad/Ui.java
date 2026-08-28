package myriad;

import java.util.List;
import java.util.Scanner;

import myriad.task.Task;
import myriad.task.TaskDateTime;

/**
 * Owns both halves of talking to the user: every line printed to the
 * console, including the divider framing that used to be repeated at the
 * top and bottom of nearly every handler in Myriad, and the reading of
 * what the user types back. One show* method per user-facing
 * interaction.
 */
public class Ui {
    private static final String LINE = "____________________________________________________________";

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates a Ui that reads from standard input and writes to standard
     * output — the console the chatbot runs in.
     */
    public Ui() {
    }

    /** Prints the horizontal rule that frames every message below. */
    private void showDivider() {
        System.out.println(LINE);
    }

    /**
     * Returns whether the user has typed anything more. False once the
     * input runs out, which happens when input is piped in from a file
     * rather than typed — the caller stops then, exactly as it would on
     * an explicit exit command, instead of failing on a missing line.
     *
     * @return true if another line of input is available.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next line the user typed, with leading and trailing
     * whitespace removed. The stripping happens here, as part of taking
     * the input in, so that everything downstream — the Parser especially
     * — can assume a tidy line.
     *
     * @return the stripped line.
     */
    public String readCommand() {
        return scanner.nextLine().strip();
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
     *
     * @param task       the task just added.
     * @param totalCount how many tasks the list now holds.
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
     *
     * @param tasks the tasks to print, in task-number order.
     */
    public void showList(List<Task> tasks) {
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
     *
     * @param matches the tasks that occur during query.
     * @param query   the date/time the user asked about, shown in the header.
     */
    public void showTasksOn(List<Task> matches, TaskDateTime query) {
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
     * Prints the tasks whose descriptions match keyword (from the "find"
     * command), 1-indexed like showList, or a "none found" message if
     * matches is empty. Framed by divider lines.
     *
     * @param matches the tasks whose descriptions contain keyword.
     * @param keyword the keyword the user searched for, shown when nothing matches.
     */
    public void showMatchingTasks(List<Task> matches, String keyword) {
        showDivider();
        if (matches.isEmpty()) {
            System.out.printf("No matching tasks found for \"%s\".%n", keyword);
        } else {
            System.out.println("Here are the matching tasks in your list:");
            for (int i = 0; i < matches.size(); i++) {
                System.out.printf("%d.%s%n", i + 1, matches.get(i));
            }
        }
        showDivider();
    }

    /**
     * Prints an acknowledgement that task was marked done, framed by
     * divider lines.
     *
     * @param task the task in its new, done state.
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
     *
     * @param task the task in its new, not-done state.
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
     *
     * @param task       the task just removed.
     * @param totalCount how many tasks the list now holds.
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
     *
     * @param message the message to print, prefix included.
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
     *
     * @param skippedLines one description per skipped line, already formatted
     *                     with its line number.
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

    /**
     * Prints a warning that the saved data file couldn't be read at all
     * (e.g. permission denied), framed by divider lines. Deliberately
     * separate from showLoadWarning: that one reports individual bad lines
     * within a file that did load, whereas this one means no task was
     * recovered and the session starts from an empty list — so the wording
     * has to warn that saving later will overwrite whatever is still in
     * that file.
     *
     * @param message why the file couldn't be read.
     */
    public void showLoadingError(String message) {
        showDivider();
        System.out.println("Warning: couldn't read your saved tasks, so I'm starting with an "
                + "empty list (saving a task later will overwrite the existing data file): "
                + message);
        showDivider();
    }
}
