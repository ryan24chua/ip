package myriad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Myriad#getResponse(String)}, the single seam a GUI talks
 * through. It has to answer a line exactly as the console loop would — same
 * wording, same error handling — while also doing the two things the loop
 * does around it: greeting first, and saying goodbye at the end.
 * <p>
 * Every test points the session at a JUnit-managed temporary data file, so
 * the real one at {@code data/myriad.txt} is never touched. No JavaFX class
 * is used here: a headless test JVM has no toolkit to load one into.
 */
public class MyriadTest {

    /** What a recorded reply puts between its lines, on every platform. */
    private static final String NEWLINE = "\n";

    /** JUnit creates this per test and deletes it afterwards. */
    @TempDir
    private Path tempDir;

    /** Path to a data file inside the temp directory, not yet created. */
    private Path dataFile() {
        return tempDir.resolve("myriad.txt");
    }

    /** A session on the temp data file, with console echoing off as a GUI has it. */
    private Myriad sessionAtTempFile() {
        return new Myriad(dataFile().toString(), false);
    }

    /** Writes the given lines to the data file, newline-separated. */
    private void writeDataFile(String... lines) throws IOException {
        Files.writeString(dataFile(), String.join("\n", lines) + "\n");
    }

    // ---------------------------------------------------------------
    // getGreeting: what run() shows before its loop
    // ---------------------------------------------------------------

    @Test
    public void getGreeting_noSavedData_greetingOnly() {
        assertEquals("Hello! I'm Myriad." + NEWLINE + "What can I do for you?",
                sessionAtTempFile().getGreeting());
    }

    @Test
    public void getGreeting_savedDataWithBadLine_warningFollowsGreeting() throws IOException {
        // A load problem is reported after the greeting, never before it.
        writeDataFile("T | 0 | read book", "nonsense");
        String greeting = sessionAtTempFile().getGreeting();
        assertTrue(greeting.startsWith("Hello! I'm Myriad."));
        assertTrue(greeting.contains("could not be loaded and were skipped"));
        assertTrue(greeting.contains("line 2"));
    }

    @Test
    public void getGreeting_calledTwice_notRepeatedWithinOneReply() {
        // Each call is its own reply, so the second must not carry the first.
        Myriad myriad = sessionAtTempFile();
        assertEquals(myriad.getGreeting(), myriad.getGreeting());
    }

    // ---------------------------------------------------------------
    // getResponse: one line in, one reply out
    // ---------------------------------------------------------------

    @Test
    public void getResponse_todoCommand_additionConfirmed() {
        assertEquals("Got it. I've added this task:" + NEWLINE
                + "[T][ ] read book" + NEWLINE
                + "Now you have 1 tasks in the list.",
                sessionAtTempFile().getResponse("todo read book"));
    }

    @Test
    public void getResponse_unrecognisedCommand_errorReturnedNotThrown() {
        // A GUI has nowhere to catch a MyriadException, so getResponse must
        // turn it into text the same way the console loop does.
        String response = sessionAtTempFile().getResponse("blah");
        assertEquals("Error: I don't recognize that command. Try: todo, deadline, event, list, "
                + "mark, unmark, delete, show, find, or bye.", response);
    }

    @Test
    public void getResponse_deleteFromEmptyList_errorReturned() {
        // Failures raised while executing, not parsing, take the same path.
        assertTrue(sessionAtTempFile().getResponse("delete 1").startsWith("Error: "));
    }

    @Test
    public void getResponse_surroundingWhitespace_commandStillRecognised() {
        // The console strips input as it reads it; a text field does not, so
        // getResponse strips on the GUI's behalf.
        assertTrue(sessionAtTempFile().getResponse("   list   ")
                .startsWith("Here are the tasks in your list:"));
    }

    @Test
    public void getResponse_secondCommand_firstReplyNotRepeated() {
        Myriad myriad = sessionAtTempFile();
        myriad.getResponse("todo read book");
        String second = myriad.getResponse("list");
        assertFalse(second.contains("Got it."));
        assertEquals("Here are the tasks in your list:" + NEWLINE + "1.[T][ ] read book", second);
    }

    @Test
    public void getResponse_blankInput_errorReturned() {
        // The GUI filters blank lines before calling, but the seam itself
        // still has to answer rather than throw.
        assertTrue(sessionAtTempFile().getResponse("   ").startsWith("Error: "));
    }

    @Test
    public void getResponse_addThenList_taskVisibleInSameSession() {
        Myriad myriad = sessionAtTempFile();
        myriad.getResponse("todo read book");
        myriad.getResponse("mark 1");
        assertEquals("Here are the tasks in your list:" + NEWLINE + "1.[T][X] read book",
                myriad.getResponse("list"));
    }

    @Test
    public void getResponse_addTask_writtenToDataFile() throws IOException {
        // A GUI session must save exactly as a console one does, since both
        // front ends share the one data file.
        sessionAtTempFile().getResponse("todo read book");
        assertEquals("T | 0 | read book\n", Files.readString(dataFile()));
    }

    // ---------------------------------------------------------------
    // Ending the session
    // ---------------------------------------------------------------

    @Test
    public void isExitRequested_beforeAnyCommand_false() {
        assertFalse(sessionAtTempFile().isExitRequested());
    }

    @Test
    public void getResponse_byeCommand_farewellReturned() {
        // run() prints the farewell after its loop because ExitCommand does
        // nothing; with no loop, getResponse has to produce it.
        assertEquals("Bye. Hope to see you again soon!", sessionAtTempFile().getResponse("bye"));
    }

    @Test
    public void isExitRequested_afterByeCommand_true() {
        Myriad myriad = sessionAtTempFile();
        myriad.getResponse("bye");
        assertTrue(myriad.isExitRequested());
    }

    @Test
    public void isExitRequested_afterOrdinaryCommand_false() {
        Myriad myriad = sessionAtTempFile();
        myriad.getResponse("todo read book");
        assertFalse(myriad.isExitRequested());
    }
}
