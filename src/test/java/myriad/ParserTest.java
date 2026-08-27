package myriad;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import myriad.command.AddCommand;
import myriad.command.Command;
import myriad.command.DeleteCommand;
import myriad.command.ExitCommand;
import myriad.command.ListCommand;
import myriad.command.MarkCommand;
import myriad.command.ShowCommand;
import myriad.command.UnmarkCommand;

/**
 * Tests for {@link Parser}, the single place that knows Myriad's command
 * language. Every line the user types passes through {@code parse}, so
 * these tests cover both the dispatch (which Command a line produces) and
 * the rejection paths (which message a malformed line produces).
 * <p>
 * Note on what is asserted: the Command classes expose no getters for the
 * values the Parser put in them, and reading those values back would mean
 * calling {@code execute}, which prints and writes to disk. So a
 * successful parse is asserted by its Command type, and the argument
 * handling is pinned down through the failure messages instead.
 */
public class ParserTest {

    /** Fails the test unless the message contains the expected fragment. */
    private static void assertMessageContains(String expectedFragment, MyriadException actual) {
        assertTrue(actual.getMessage().contains(expectedFragment),
                "expected message to contain \"" + expectedFragment + "\" but was: "
                        + actual.getMessage());
    }

    /** Parses a line that is expected to fail, returning the exception. */
    private static MyriadException parseExpectingFailure(String line) {
        return assertThrows(MyriadException.class, () -> Parser.parse(line));
    }

    // ---------------------------------------------------------------
    // Dispatch: each keyword produces its Command
    // ---------------------------------------------------------------

    @Test
    public void parse_bye_exitCommandReturned() throws MyriadException {
        Command command = Parser.parse("bye");
        assertInstanceOf(ExitCommand.class, command);
        assertTrue(command.isExit(), "bye must be the command that ends the loop");
    }

    @Test
    public void parse_list_listCommandReturned() throws MyriadException {
        Command command = Parser.parse("list");
        assertInstanceOf(ListCommand.class, command);
        assertTrue(!command.isExit(), "list must not end the loop");
    }

    @Test
    public void parse_mark_markCommandReturned() throws MyriadException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 2"));
    }

    @Test
    public void parse_unmark_unmarkCommandReturned() throws MyriadException {
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 2"));
    }

    @Test
    public void parse_delete_deleteCommandReturned() throws MyriadException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 2"));
    }

    @Test
    public void parse_todo_addCommandReturned() throws MyriadException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
    }

    @Test
    public void parse_deadline_addCommandReturned() throws MyriadException {
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /by 2019-12-02"));
    }

    @Test
    public void parse_event_addCommandReturned() throws MyriadException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("event party /from 2019-12-02 1400 /to 2019-12-02 1600"));
    }

    @Test
    public void parse_show_showCommandReturned() throws MyriadException {
        assertInstanceOf(ShowCommand.class, Parser.parse("show 2019-12-02"));
    }

    // ---------------------------------------------------------------
    // Keyword matching
    // ---------------------------------------------------------------

    @Test
    public void parse_keywordInUpperCase_stillRecognised() throws MyriadException {
        assertInstanceOf(ExitCommand.class, Parser.parse("BYE"));
        assertInstanceOf(ListCommand.class, Parser.parse("LIST"));
        assertInstanceOf(AddCommand.class, Parser.parse("TODO read book"));
    }

    @Test
    public void parse_keywordInMixedCase_stillRecognised() throws MyriadException {
        assertInstanceOf(MarkCommand.class, Parser.parse("MaRk 2"));
    }

    @Test
    public void parse_unknownKeyword_exceptionThrown() {
        MyriadException e = parseExpectingFailure("blah");
        assertMessageContains("I don't recognize that command", e);
    }

    @Test
    public void parse_unknownKeyword_messageListsEveryKeyword() {
        // If a command is ever added, this catches a help message that was
        // not updated alongside it.
        String message = parseExpectingFailure("blah").getMessage();
        for (String keyword : new String[] {
            "todo", "deadline", "event", "list", "mark", "unmark", "delete", "show", "bye",
        }) {
            assertTrue(message.contains(keyword), "help message omits \"" + keyword + "\"");
        }
    }

    @Test
    public void parse_keywordAsPrefixOfLongerWord_notRecognised() {
        // "listen" must not be treated as "list"; the keyword is matched
        // against the whole first word, not a prefix of it.
        assertMessageContains("I don't recognize that command", parseExpectingFailure("listen"));
    }

    // ---------------------------------------------------------------
    // Commands that take no arguments
    // ---------------------------------------------------------------

    @Test
    public void parse_byeWithTrailingArgument_notTreatedAsExit() {
        // "bye" only matches when it is the whole line, so a stray argument
        // must not quietly end the session.
        assertMessageContains("I don't recognize that command", parseExpectingFailure("bye now"));
    }

    @Test
    public void parse_listWithTrailingArgument_notTreatedAsList() {
        assertMessageContains("I don't recognize that command", parseExpectingFailure("list 1"));
    }

    // ---------------------------------------------------------------
    // Preconditions on the input line
    // ---------------------------------------------------------------

    @Test
    public void parse_emptyLine_exceptionThrown() {
        assertMessageContains("I don't recognize that command", parseExpectingFailure(""));
    }

    @Test
    public void parse_leadingWhitespace_notRecognised() {
        // parse() expects an already-stripped line (Ui.readCommand strips
        // before calling). With leading whitespace the split yields an empty
        // first word, so even a valid command falls through to the unknown
        // branch. Documented so the precondition is not lost.
        assertMessageContains("I don't recognize that command", parseExpectingFailure(" todo read book"));
    }

    @Test
    public void parse_repeatedSpacesBetweenKeywordAndArguments_accepted() throws MyriadException {
        // The split is on a whitespace run, so extra spacing is harmless.
        assertInstanceOf(AddCommand.class, Parser.parse("todo    read book"));
    }

    // ---------------------------------------------------------------
    // Task number arguments (mark / unmark / delete)
    // ---------------------------------------------------------------

    @Test
    public void parse_markWithoutNumber_exceptionThrown() {
        assertMessageContains("Please tell me which task number", parseExpectingFailure("mark"));
    }

    @Test
    public void parse_deleteWithoutNumber_exceptionThrown() {
        assertMessageContains("Please tell me which task number", parseExpectingFailure("delete"));
    }

    @Test
    public void parse_markWithNonNumericArgument_exceptionThrown() {
        MyriadException e = parseExpectingFailure("mark two");
        assertMessageContains("is not a valid task number", e);
        assertMessageContains("two", e);
    }

    @Test
    public void parse_markWithDecimalNumber_exceptionThrown() {
        assertMessageContains("is not a valid task number", parseExpectingFailure("mark 2.0"));
    }

    @Test
    public void parse_markWithTwoNumbers_exceptionThrown() {
        assertMessageContains("is not a valid task number", parseExpectingFailure("mark 2 3"));
    }

    @Test
    public void parse_markWithNumberTooLargeForInt_exceptionThrown() {
        // Integer.parseInt overflows rather than saturating, so this is a
        // rejection, not a silently wrong task number.
        assertMessageContains("is not a valid task number", parseExpectingFailure("mark 99999999999"));
    }

    @Test
    public void parse_markWithNegativeNumber_parsedNotRejected() {
        // Whether a number is in range depends on the task list at the moment
        // the command runs, so the Parser deliberately accepts any whole
        // number and leaves the range check to
        // TaskNumberCommand.resolveIndex. Recorded here so this is read as
        // intentional rather than as a missing validation.
        assertDoesNotThrow(() -> Parser.parse("mark -1"));
    }

    @Test
    public void parse_markWithZero_parsedNotRejected() {
        assertDoesNotThrow(() -> Parser.parse("mark 0"));
    }

    @Test
    public void parse_markWithExplicitlySignedNumber_parsedNotRejected() {
        // Integer.parseInt accepts a leading "+".
        assertDoesNotThrow(() -> Parser.parse("mark +2"));
    }

    // ---------------------------------------------------------------
    // todo
    // ---------------------------------------------------------------

    @Test
    public void parse_todoWithoutDescription_exceptionThrown() {
        MyriadException e = parseExpectingFailure("todo");
        assertMessageContains("Please include a task description", e);
        assertMessageContains("todo read book", e);
    }

    // ---------------------------------------------------------------
    // deadline
    // ---------------------------------------------------------------

    @Test
    public void parse_deadlineWithoutDescription_descriptionErrorThrown() {
        assertMessageContains("Please include a task description",
                parseExpectingFailure("deadline /by 2019-12-02"));
    }

    @Test
    public void parse_deadlineWithoutByMarker_dateErrorThrown() {
        assertMessageContains("Please include a date after /by",
                parseExpectingFailure("deadline return book"));
    }

    @Test
    public void parse_deadlineWithEmptyDate_dateErrorThrown() {
        assertMessageContains("Please include a date after /by",
                parseExpectingFailure("deadline return book /by"));
    }

    @Test
    public void parse_deadlineMissingBothParts_descriptionCheckedFirst() {
        // Both are missing; the description error is the one reported.
        assertMessageContains("Please include a task description",
                parseExpectingFailure("deadline /by"));
    }

    @Test
    public void parse_deadlineWithUnparseableDate_dateFormatErrorThrown() {
        // The date is handed to TaskDateTime.parse, so a word like "monday"
        // is rejected with the date-format message rather than accepted as
        // free text.
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("deadline return book /by monday"));
    }

    @Test
    public void parse_deadlineWithUppercaseMarker_accepted() throws MyriadException {
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /BY 2019-12-02"));
    }

    @Test
    public void parse_deadlineWithNoSpaceAroundMarker_accepted() throws MyriadException {
        // The marker regex consumes optional surrounding whitespace, so the
        // spaces are a convenience rather than a requirement.
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book/by 2019-12-02"));
    }

    @Test
    public void parse_deadlineWithSecondByMarker_dateFormatErrorThrown() {
        // The split has a limit of 2, so only the first marker separates the
        // parts; everything after it -- second marker included -- is the date
        // text, which then fails to parse.
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("deadline return book /by tomorrow /by 2019-12-02"));
    }

    @Test
    public void parse_deadlineWithMarkerInsideLongerWord_markerStillMatches() {
        // Known limitation: the marker regex has no word boundary, so "/by"
        // matches the start of "/byebye". The description ends at "return
        // book" and the date text becomes "bye 2019-12-02", which then fails
        // to parse -- a confusing message for what is really a typo.
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("deadline return book /byebye 2019-12-02"));
    }

    // ---------------------------------------------------------------
    // event
    // ---------------------------------------------------------------

    @Test
    public void parse_eventWithoutDescription_descriptionErrorThrown() {
        assertMessageContains("Please include a task description",
                parseExpectingFailure("event /from 2019-12-02 1400 /to 2019-12-02 1600"));
    }

    @Test
    public void parse_eventWithoutStart_startErrorThrown() {
        assertMessageContains("Please include a start time after /from",
                parseExpectingFailure("event party /from /to 2019-12-02 1600"));
    }

    @Test
    public void parse_eventWithoutToMarker_endErrorThrown() {
        assertMessageContains("Please include an end time after /to",
                parseExpectingFailure("event party /from 2019-12-02 1400"));
    }

    @Test
    public void parse_eventWithoutBothMarkers_startErrorThrown() {
        // With no "/from" the remaining text is empty, so the start check is
        // the first one to fail.
        assertMessageContains("Please include a start time after /from",
                parseExpectingFailure("event party"));
    }

    @Test
    public void parse_eventWithMarkersInWrongOrder_endErrorThrown() {
        // "/from" is split off first, so everything before it -- "/to" and
        // all -- becomes the description, and the end time is then reported
        // missing even though the user did type "/to". Worth pinning down:
        // the message names a marker the user supplied.
        assertMessageContains("Please include an end time after /to",
                parseExpectingFailure("event party /to 2019-12-02 1600 /from 2019-12-02 1400"));
    }

    @Test
    public void parse_eventWithUnparseableStart_dateFormatErrorThrown() {
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("event party /from friday /to 2019-12-02 1600"));
    }

    @Test
    public void parse_eventWithUnparseableEnd_dateFormatErrorThrown() {
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("event party /from 2019-12-02 1400 /to saturday"));
    }

    @Test
    public void parse_eventWithMixedCaseMarkers_accepted() throws MyriadException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("event party /From 2019-12-02 1400 /TO 2019-12-02 1600"));
    }

    @Test
    public void parse_eventEndingBeforeItStarts_acceptedNotRejected() {
        // Known limitation: the Parser checks that both times are present and
        // parseable, but nothing checks that the start is not after the end,
        // so an impossible event is accepted here and never matches a "show"
        // query later.
        assertDoesNotThrow(() ->
                Parser.parse("event party /from 2019-12-05 /to 2019-12-01"));
    }

    // ---------------------------------------------------------------
    // show
    // ---------------------------------------------------------------

    @Test
    public void parse_showWithoutQuery_exceptionThrown() {
        assertMessageContains("Please include a date/time to show", parseExpectingFailure("show"));
    }

    @Test
    public void parse_showWithUnparseableQuery_dateFormatErrorThrown() {
        assertMessageContains("doesn't look like a date/time I understand",
                parseExpectingFailure("show someday"));
    }

    @Test
    public void parse_showWithDateAndTime_accepted() throws MyriadException {
        assertInstanceOf(ShowCommand.class, Parser.parse("show 2019-12-02 1800"));
    }

    @Test
    public void parse_showWithSlashFormatDate_accepted() throws MyriadException {
        assertInstanceOf(ShowCommand.class, Parser.parse("show 2/12/2019"));
    }
}
