package myriad;

import java.io.File;
import java.util.List;

import myriad.command.Command;

/**
 * Entry point for the Myriad chatbot.
 * Loads any previously saved tasks from disk, greets the user, then reads
 * lines of input, each treated as a command:
 * add a task ("todo"/"deadline"/"event"), "list" the stored tasks,
 * "mark"/"unmark" a task done, until the user types the exit command
 * ("bye"), then prints a farewell. A line that doesn't match any known
 * command, or that's missing a required argument, throws a
 * MyriadException, which is caught once per line in readCommands() and
 * shown as an error.
 *
 * One chatbot session is one Myriad object: it holds the pieces that
 * session needs — the Ui it talks through, the Storage it saves to, and
 * the TaskList it works on — and hands all three to each Command it runs.
 * Working out what a line means is the Parser's job and carrying it out
 * is the Command's, so this class is left with the wiring: set the three
 * up, then feed lines through them.
 */
public class Myriad {

    private final Ui ui;
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Descriptions of any saved-data lines that couldn't be loaded, or null
     * if the whole file couldn't be read. Both are held from construction
     * until run() shows them, because a load problem has to be reported
     * after the greeting rather than before it.
     */
    private final List<String> skippedLines;
    private final String loadErrorMessage;

    /**
     * Whether a command has asked for the session to end. Only a GUI needs
     * this: the console loop learns the same thing from Command.isExit(),
     * but a GUI only ever sees the reply text, so the request is recorded
     * here for it to act on.
     */
    private boolean isExitRequested = false;

    /**
     * Sets up one chatbot session: creates the Ui, points Storage at
     * filePath, and loads whatever tasks were saved there last session. A
     * file that can't be read at all isn't fatal — the session starts from
     * an empty list and run() warns about it — so that a single unreadable
     * file doesn't stop the user from using the chatbot at all.
     *
     * @param filePath path to the data file this session loads from and
     *                 saves to.
     */
    public Myriad(String filePath) {
        this(filePath, true);
    }

    /**
     * Sets up one chatbot session, as the single-argument constructor does,
     * but lets the caller say whether messages are printed to the console.
     * A GUI session passes false: it shows the reply in a dialog box instead.
     *
     * @param filePath           path to the data file this session loads from
     *                           and saves to.
     * @param isEchoingToConsole whether messages are also printed to standard output.
     */
    public Myriad(String filePath, boolean isEchoingToConsole) {
        this.ui = new Ui(isEchoingToConsole);
        this.storage = new Storage(filePath);

        TaskList loadedTasks;
        List<String> loadedSkippedLines;
        String errorMessage;
        try {
            LoadResult loaded = storage.load();
            loadedTasks = new TaskList(loaded.tasks());
            loadedSkippedLines = loaded.skippedLines();
            errorMessage = null;
        } catch (MyriadException e) {
            loadedTasks = new TaskList();
            loadedSkippedLines = List.of();
            errorMessage = e.getMessage();
        }
        this.tasks = loadedTasks;
        this.skippedLines = loadedSkippedLines;
        this.loadErrorMessage = errorMessage;
    }

    /**
     * Runs one chatbot session: greets the user, warns about anything
     * that couldn't be loaded, then takes lines from the Ui until a
     * command says the session is over or the input runs out, and finally
     * says goodbye. Each line is carried out by executeLine, the same
     * method the GUI goes through, so whether to stop is the command's
     * answer rather than a keyword this loop checks for.
     */
    public void run() {
        showStartupMessages();

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            // Each line is a reply of its own, so the recorded text is cleared
            // rather than left to grow for the whole session.
            ui.startResponse();
            isExit = executeLine(ui.readCommand());
        }
        ui.showFarewell();
    }

    /**
     * Returns the opening message of a GUI session: the greeting, followed by
     * any warning about saved data that could not be read. This is what run()
     * shows before its loop, packaged as text because a GUI has no loop to
     * hang it off.
     *
     * @return the greeting and any load warnings.
     */
    public String getGreeting() {
        ui.startResponse();
        showStartupMessages();
        return ui.getResponse();
    }

    /**
     * Shows the greeting, then any warning about saved data that could not
     * be loaded. Shared by run() and getGreeting() so the console and the
     * GUI always open a session with the same messages.
     */
    private void showStartupMessages() {
        ui.showGreeting();
        if (loadErrorMessage != null) {
            ui.showLoadingError(loadErrorMessage);
        }
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }
    }

    /**
     * Runs one line of user input and returns what the chatbot says back.
     * Goes through executeLine, as each iteration of run() does, so that the
     * GUI and the console answer any given line the same way.
     *
     * @param input the raw line the user typed, whitespace included.
     * @return the chatbot's reply.
     */
    public String getResponse(String input) {
        ui.startResponse();
        // Stripped here because readCommand() does it for the console, and
        // Parser expects a tidy line from either front end.
        boolean isExit = executeLine(input.strip());
        if (isExit) {
            // run() says goodbye after its loop, since ExitCommand.execute
            // is empty; with no loop, the farewell belongs in the reply.
            ui.showFarewell();
            isExitRequested = true;
        }
        return ui.getResponse();
    }

    /**
     * Parses and carries out one stripped line of input, showing any error
     * through the Ui, and returns whether the command asked to end the
     * session.
     *
     * Both parsing and executing throw MyriadException instead of showing
     * an error themselves, so this is the single place that catches it and
     * shows it, with the "Error: " prefix added here rather than repeated
     * in every message. Save failures arrive as MyriadException too, so
     * they are reported like any other command error instead of crashing
     * the program.
     *
     * @param strippedLine one line of input, already whitespace-stripped.
     * @return true if the command was an exit command; false otherwise,
     *         including when the line was rejected with an error.
     */
    private boolean executeLine(String strippedLine) {
        try {
            Command command = Parser.parse(strippedLine);
            command.execute(tasks, ui, storage);
            return command.isExit();
        } catch (MyriadException e) {
            ui.showError("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns whether a command has ended the session, so that a GUI knows to
     * close its window.
     *
     * @return true once an exit command has been run.
     */
    public boolean isExitRequested() {
        return isExitRequested;
    }

    /**
     * Starts one chatbot session reading and writing data/myriad.txt.
     *
     * @param args ignored; the data file location is fixed here rather than
     *             taken from the command line.
     */
    public static void main(String[] args) {
        // Built with File rather than a "data/myriad.txt" literal so the
        // separator is right on every OS.
        new Myriad(new File("data", "myriad.txt").getPath()).run();
    }
}
