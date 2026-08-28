package myriad;

import myriad.command.AddCommand;
import myriad.command.Command;
import myriad.command.DeleteCommand;
import myriad.command.ExitCommand;
import myriad.command.FindCommand;
import myriad.command.ListCommand;
import myriad.command.MarkCommand;
import myriad.command.ShowCommand;
import myriad.command.UnmarkCommand;
import myriad.task.Deadline;
import myriad.task.Event;
import myriad.task.Task;
import myriad.task.TaskDateTime;
import myriad.task.ToDo;

/**
 * Makes sense of what the user typed, turning a line of input into the
 * Command that carries it out. This is the one place that knows the
 * command language — the keywords, the "/by", "/from" and "/to" markers,
 * and which arguments each command requires — so nothing else has to
 * look at the raw line at all.
 *
 * The methods are static because parsing a line depends only on that
 * line: there's nothing to remember between calls, so a Parser object
 * would carry no state and only add ceremony at every call site.
 */
public class Parser {

    /** Not meant to be instantiated: every method here is static. */
    private Parser() {
    }

    /**
     * Builds the Command a stripped input line asks for, with that line's
     * arguments already interpreted. The first word is matched
     * case-insensitively against the known command keywords. "bye" and
     * "list" take no arguments, so they only match when they are the
     * whole line. Throws MyriadException if the line names no known
     * command, or if its arguments can't be made sense of — so a command
     * object only ever exists if it can actually be attempted.
     *
     * @param strippedLine one line of user input, already whitespace-stripped
     *                     by Ui.readCommand.
     * @return the Command that line asks for.
     * @throws MyriadException if the line names no known command, or its
     *                         arguments are missing or unparseable.
     */
    public static Command parse(String strippedLine) throws MyriadException {
        String firstWord = strippedLine.split("\\s+", 2)[0];
        String args = extractArguments(strippedLine);

        if (firstWord.equalsIgnoreCase("bye") && args.isEmpty()) {
            return new ExitCommand();
        } else if (firstWord.equalsIgnoreCase("list") && args.isEmpty()) {
            return new ListCommand();
        } else if (firstWord.equalsIgnoreCase("mark")) {
            return new MarkCommand(parseTaskNumber(args));
        } else if (firstWord.equalsIgnoreCase("unmark")) {
            return new UnmarkCommand(parseTaskNumber(args));
        } else if (firstWord.equalsIgnoreCase("todo")) {
            return new AddCommand(parseToDo(args));
        } else if (firstWord.equalsIgnoreCase("deadline")) {
            return new AddCommand(parseDeadline(args));
        } else if (firstWord.equalsIgnoreCase("event")) {
            return new AddCommand(parseEvent(args));
        } else if (firstWord.equalsIgnoreCase("delete")) {
            return new DeleteCommand(parseTaskNumber(args));
        } else if (firstWord.equalsIgnoreCase("show")) {
            return new ShowCommand(parseShowQuery(args));
        } else if (firstWord.equalsIgnoreCase("find")) {
            return new FindCommand(parseFindKeyword(args));
        } else {
            throw new MyriadException(
                    "I don't recognize that command. Try: todo, deadline, event, "
                            + "list, mark, unmark, delete, show, find, or bye.");
        }
    }

    /**
     * Returns everything after the command keyword in a stripped line
     * (e.g. "read book" from "todo read book"), or an empty string if the
     * line is the keyword alone. Every parse* method below takes this
     * argument text rather than the whole line, so the keyword is split
     * off exactly once per line.
     *
     * @param strippedLine one whole line of input.
     * @return the argument text, possibly empty, never null.
     */
    private static String extractArguments(String strippedLine) {
        String[] parts = strippedLine.split("\\s+", 2);
        return parts.length == 2 ? parts[1] : "";
    }

    /**
     * Parses the task number argument of a mark/unmark/delete command
     * (e.g. the "2" in "mark 2") and returns it as typed, 1-based. Throws
     * MyriadException if it is missing or isn't a whole number.
     *
     * Deliberately stops there: whether that number actually exists
     * depends on how many tasks there are right now, which is the task
     * list's business, not the command language's — so the range check
     * happens later, when the command runs (see
     * TaskNumberCommand.resolveIndex).
     *
     * @param args the argument text after the keyword.
     * @return the number as typed, 1-based.
     * @throws MyriadException if args is empty or isn't a whole number.
     */
    private static int parseTaskNumber(String args) throws MyriadException {
        if (args.isEmpty()) {
            throw new MyriadException(
                    "Please tell me which task number, e.g. \"mark 2\".");
        }

        String arg = args.strip();
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new MyriadException(
                    "\"" + arg + "\" is not a valid task number — it needs to be a whole "
                            + "number, e.g. \"mark 2\".");
        }
    }

    /**
     * Builds the ToDo described by a "todo" command's arguments. Throws
     * MyriadException if the description is missing.
     *
     * @param args the argument text after "todo".
     * @return the new ToDo.
     * @throws MyriadException if the description is missing.
     */
    private static Task parseToDo(String args) throws MyriadException {
        if (args.isEmpty()) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"todo read book\".");
        }
        return new ToDo(args);
    }

    /**
     * Splits text on the given marker (e.g. "/by"), matched
     * case-insensitively with optional surrounding whitespace consumed —
     * this mirrors the case-insensitive matching used for command keywords
     * elsewhere (e.g. "deadline" itself). Returns a 1-element array holding
     * all of the text if the marker isn't found, or a 2-element array of the
     * text before/after the marker if it is.
     *
     * @param text   the text to split.
     * @param marker the marker to split on, e.g. "/by".
     * @return a 1- or 2-element array, as described above.
     */
    private static String[] splitOnMarker(String text, String marker) {
        return text.split("(?i)\\s*" + marker + "\\s*", 2);
    }

    /**
     * Builds the Deadline described by a "deadline &lt;description&gt; /by
     * &lt;date&gt;" command's arguments. Throws MyriadException if the
     * description or the date is missing, or if the date doesn't parse.
     *
     * @param args the argument text after "deadline".
     * @return the new Deadline.
     * @throws MyriadException if the description or date is missing or
     *                         unparseable.
     */
    private static Task parseDeadline(String args) throws MyriadException {
        String[] descAndDate = splitOnMarker(args, "/by");
        String description = descAndDate[0];
        String date = descAndDate.length == 2 ? descAndDate[1] : null;

        String example = "deadline return book /by 2019-12-02";
        if (description.isBlank()) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"" + example + "\".");
        } else if (date == null || date.isBlank()) {
            throw new MyriadException(
                    "Please include a date after /by, e.g. \"" + example + "\".");
        }
        return new Deadline(description, TaskDateTime.parse(date));
    }

    /**
     * Builds the Event described by an "event &lt;description&gt; /from
     * &lt;start&gt; /to &lt;end&gt;" command's arguments. Throws
     * MyriadException if the description, start or end is missing, or if
     * either date doesn't parse.
     *
     * @param args the argument text after "event".
     * @return the new Event.
     * @throws MyriadException if the description, start or end is missing or
     *                         unparseable.
     */
    private static Task parseEvent(String args) throws MyriadException {
        String[] descAndRest = splitOnMarker(args, "/from");
        String description = descAndRest[0];
        String rest = descAndRest.length == 2 ? descAndRest[1] : "";

        String[] startAndEnd = splitOnMarker(rest, "/to");
        String start = startAndEnd[0];
        String end = startAndEnd.length == 2 ? startAndEnd[1] : null;

        String example = "event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600";
        if (description.isBlank()) {
            throw new MyriadException(
                    "Please include a task description, e.g. \"" + example + "\".");
        } else if (start.isBlank()) {
            throw new MyriadException(
                    "Please include a start time after /from, e.g. \"" + example + "\".");
        } else if (end == null || end.isBlank()) {
            throw new MyriadException(
                    "Please include an end time after /to, e.g. \"" + example + "\".");
        }
        return new Event(description, TaskDateTime.parse(start), TaskDateTime.parse(end));
    }

    /**
     * Parses the date/time a "show" command asks about. Throws
     * MyriadException if it is missing or doesn't parse.
     *
     * @param args the argument text after "show".
     * @return the date/time to search for.
     * @throws MyriadException if it is missing or unparseable.
     */
    private static TaskDateTime parseShowQuery(String args) throws MyriadException {
        String example = "show 2019-12-02 1800";

        if (args.isBlank()) {
            throw new MyriadException(
                    "Please include a date/time to show, e.g. \"" + example + "\".");
        }
        return TaskDateTime.parse(args.strip());
    }

    /**
     * Parses the keyword a "find" command searches for. Throws
     * MyriadException if it is missing.
     *
     * @param args the argument text after "find".
     * @return the keyword to search for, stripped of surrounding whitespace.
     * @throws MyriadException if the keyword is missing or blank.
     */
    private static String parseFindKeyword(String args) throws MyriadException {
        if (args.isBlank()) {
            throw new MyriadException(
                    "Please include a keyword to find, e.g. \"find book\".");
        }
        return args.strip();
    }
}
