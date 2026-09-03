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
     * says goodbye.
     *
     * Each line is handed to the Parser, which returns the Command it
     * asks for; carrying that command out is the command's own business,
     * so this loop doesn't need to know which commands exist, and whether
     * to stop is the command's answer (isExit) rather than a keyword this
     * method checks for. Both parsing and executing may throw
     * MyriadException instead of displaying an error themselves, so this
     * is the single place that catches it and shows it — with the
     * "Error: " prefix added here rather than repeated in every message.
     * Commands also raise MyriadException (rather than a checked
     * IOException) when saving to disk fails, so a save failure is caught
     * and shown the same way as any other command error, instead of
     * crashing the program.
     */
    public void run() {
        ui.showGreeting();
        if (loadErrorMessage != null) {
            ui.showLoadingError(loadErrorMessage);
        }
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            // Each line is a reply of its own, so the recorded text is cleared
            // rather than left to grow for the whole session.
            ui.startResponse();
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (MyriadException e) {
                ui.showError("Error: " + e.getMessage());
            }
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
        ui.showGreeting();
        if (loadErrorMessage != null) {
            ui.showLoadingError(loadErrorMessage);
        }
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }
        return ui.getResponse();
    }

    /**
     * Runs one line of user input and returns what the chatbot says back.
     * Mirrors one iteration of run(), including the single catch that turns a
     * MyriadException into an error message, so that the GUI and the console
     * answer any given line the same way.
     *
     * @param input the raw line the user typed, whitespace included.
     * @return the chatbot's reply.
     */
    public String getResponse(String input) {
        ui.startResponse();
        try {
            // Stripped here because readCommand() does it for the console, and
            // Parser expects a tidy line from either front end.
            Command command = Parser.parse(input.strip());
            command.execute(tasks, ui, storage);
            if (command.isExit()) {
                // run() says goodbye after its loop, since ExitCommand.execute
                // is empty; with no loop, the farewell belongs in the reply.
                ui.showFarewell();
                isExitRequested = true;
            }
        } catch (MyriadException e) {
            ui.showError("Error: " + e.getMessage());
        }
        return ui.getResponse();
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
