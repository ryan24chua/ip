package myriad;

import java.util.List;
import java.util.Scanner;

import myriad.task.Task;
import myriad.task.TaskDateTime;

/**
 * Owns both halves of talking to the user: every message the chatbot says,
 * including the divider framing that used to be repeated at the top and
 * bottom of nearly every handler in Myriad, and the reading of what the
 * user types back. One show* method per user-facing interaction.
 *
 * Each message is recorded into a buffer as well as (optionally) printed,
 * so the same Ui serves both front ends: the console session prints as it
 * goes, while the GUI calls startResponse(), runs a command, and reads the
 * whole reply back with getResponse().
 */
public class Ui {
    private static final String LINE = "____________________________________________________________";
    private static final String BANNER = "███╗   ███╗██╗   ██╗██████╗ ██╗ █████╗ ██████╗ \n"
            + "████╗ ████║╚██╗ ██╔╝██╔══██╗██║██╔══██╗██╔══██╗\n"
            + "██╔████╔██║ ╚████╔╝ ██████╔╝██║███████║██║  ██║\n"
            + "██║╚██╔╝██║  ╚██╔╝  ██╔══██╗██║██╔══██║██║  ██║\n"
            + "██║ ╚═╝ ██║   ██║   ██║  ██║██║██║  ██║██████╔╝\n"
            + "╚═╝     ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═════╝ ";

    /** Text of every message shown since the last startResponse call. */
    private final StringBuilder responseBuffer = new StringBuilder();

    /** Whether each message is also printed, divider-framed, to standard output. */
    private final boolean isEchoingToConsole;

    /**
     * Reader for typed input, created on first use so that a GUI session —
     * which never reads a command — does not open a Scanner on a standard
     * input stream that may not exist.
     */
    private Scanner scanner;

    /**
     * Creates a Ui for a console session: one that reads standard input and
     * prints every message to standard output.
     */
    public Ui() {
        this(true);
    }

    /**
     * Creates a Ui that records every message so the caller can read a whole
     * reply back as one String, and additionally prints each message to the
     * console when isEchoingToConsole is true.
     *
     * @param isEchoingToConsole whether messages are also printed to standard output.
     */
    public Ui(boolean isEchoingToConsole) {
        this.isEchoingToConsole = isEchoingToConsole;
    }

    /**
     * Returns the reader for typed input, opening it on the first call.
     *
     * @return the Scanner reading standard input.
     */
    private Scanner getScanner() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
    }

    /**
     * Records one message as part of the reply being built, and prints it
     * between divider lines when this Ui echoes to the console.
     *
     * @param consoleOnlyText text printed above the message on the console but
     *                        left out of the recorded reply, or null when there
     *                        is none.
     * @param message         the message text, without divider lines.
     */
    private void emit(String consoleOnlyText, String message) {
        if (responseBuffer.length() > 0) {
            responseBuffer.append(System.lineSeparator());
        }
        responseBuffer.append(message);

        if (isEchoingToConsole) {
            System.out.println(LINE);
            if (consoleOnlyText != null) {
                System.out.println(consoleOnlyText);
            }
            System.out.println(message);
            System.out.println(LINE);
        }
    }

    /**
     * Records one message that is shown the same way on the console and in a GUI.
     *
     * @param message the message text, without divider lines.
     */
    private void emit(String message) {
        emit(null, message);
    }

    /**
     * Numbers every task in tasks from 1, one per line, in the format shared
     * by the list, show and find commands.
     *
     * @param tasks the tasks to number, in task-number order.
     * @return the numbered lines, joined by line separators.
     */
    private static String formatNumberedTasks(List<Task> tasks) {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            lines.append(System.lineSeparator());
            lines.append(String.format("%d.%s", i + 1, tasks.get(i)));
        }
        return lines.toString();
    }

    /**
     * Discards any recorded text, so that the messages shown from now on form
     * a fresh reply.
     */
    public void startResponse() {
        responseBuffer.setLength(0);
    }

    /**
     * Returns every message shown since the last startResponse call, as one
     * block of text with no divider lines.
     *
     * @return the recorded reply.
     */
    public String getResponse() {
        return responseBuffer.toString();
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
        return getScanner().hasNextLine();
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
        return getScanner().nextLine().strip();
    }

    /**
     * Shows the welcome message. The ASCII banner above it is console-only:
     * its box-drawing characters only line up in a monospaced font, so a GUI
     * gets the greeting on its own.
     */
    public void showGreeting() {
        String greeting = """
                Hello! I'm Myriad.
                What can I do for you?""";
        emit(BANNER, greeting);
    }

    /**
     * Shows a farewell message.
     */
    public void showFarewell() {
        emit("Bye. Hope to see you again soon!");
    }

    /**
     * Shows the standard added-task acknowledgement: the task's own toString,
     * plus the new list size.
     *
     * @param task       the task just added.
     * @param totalCount how many tasks the list now holds.
     */
    public void showAddedTask(Task task, int totalCount) {
        emit("Got it. I've added this task:" + System.lineSeparator()
                + task + System.lineSeparator()
                + String.format("Now you have %d tasks in the list.", totalCount));
    }

    /**
     * Shows a header line followed by every task as a 1-indexed numbered list.
     *
     * @param tasks the tasks to show, in task-number order.
     */
    public void showList(List<Task> tasks) {
        emit("Here are the tasks in your list:" + formatNumberedTasks(tasks));
    }

    /**
     * Shows the tasks occurring during query (from the "show task" command),
     * 1-indexed like showList, or a "none found" message if matches is empty.
     *
     * @param matches the tasks that occur during query.
     * @param query   the date/time the user asked about, shown in the header.
     */
    public void showTasksOn(List<Task> matches, TaskDateTime query) {
        if (matches.isEmpty()) {
            emit(String.format("No deadlines or events found on %s.", query));
        } else {
            emit(String.format("Here are the tasks occurring on %s:", query) + formatNumberedTasks(matches));
        }
    }

    /**
     * Shows the tasks whose descriptions match keyword (from the "find"
     * command), 1-indexed like showList, or a "none found" message if
     * matches is empty.
     *
     * @param matches the tasks whose descriptions contain keyword.
     * @param keyword the keyword the user searched for, shown when nothing matches.
     */
    public void showMatchingTasks(List<Task> matches, String keyword) {
        if (matches.isEmpty()) {
            emit(String.format("No matching tasks found for \"%s\".", keyword));
        } else {
            emit("Here are the matching tasks in your list:" + formatNumberedTasks(matches));
        }
    }

    /**
     * Shows an acknowledgement that task was marked done.
     *
     * @param task the task in its new, done state.
     */
    public void showMarked(Task task) {
        emit("Nice! I've marked this task as done:" + System.lineSeparator() + "  " + task);
    }

    /**
     * Shows an acknowledgement that task was marked not done.
     *
     * @param task the task in its new, not-done state.
     */
    public void showUnmarked(Task task) {
        emit("OK, I've marked this task as not done yet:" + System.lineSeparator() + "  " + task);
    }

    /**
     * Shows an acknowledgement that task was removed: the task's own toString,
     * plus the new list size.
     *
     * @param task       the task just removed.
     * @param totalCount how many tasks the list now holds.
     */
    public void showDeleted(Task task, int totalCount) {
        emit("Noted. I've removed this task:" + System.lineSeparator()
                + "  " + task + System.lineSeparator()
                + String.format("Now you have %d tasks in the list.", totalCount));
    }

    /**
     * Shows a single-line error/status message.
     *
     * @param message the message to show, prefix included.
     */
    public void showError(String message) {
        emit(message);
    }

    /**
     * Shows a consolidated warning that some lines in the saved data file
     * could not be loaded and were skipped. Each entry in skippedLines is one
     * already-formatted line description; the other, valid tasks from the file
     * are unaffected and already in the task list by the time this is called.
     *
     * @param skippedLines one description per skipped line, already formatted
     *                     with its line number.
     */
    public void showLoadWarning(List<String> skippedLines) {
        StringBuilder message = new StringBuilder(String.format(
                "Warning: %d line(s) in your saved data could not be loaded and were skipped:",
                skippedLines.size()));
        for (String skipped : skippedLines) {
            message.append(System.lineSeparator());
            message.append("  - ").append(skipped);
        }
        emit(message.toString());
    }

    /**
     * Shows a warning that the saved data file couldn't be read at all (e.g.
     * permission denied). Deliberately separate from showLoadWarning: that one
     * reports individual bad lines within a file that did load, whereas this
     * one means no task was recovered and the session starts from an empty
     * list — so the wording has to warn that saving later will overwrite
     * whatever is still in that file.
     *
     * @param message why the file couldn't be read.
     */
    public void showLoadingError(String message) {
        emit("Warning: couldn't read your saved tasks, so I'm starting with an "
                + "empty list (saving a task later will overwrite the existing data file): "
                + message);
    }
}
