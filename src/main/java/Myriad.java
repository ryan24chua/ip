import java.io.File;
import java.util.List;

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
     * Sets up one chatbot session: creates the Ui, points Storage at
     * filePath, and loads whatever tasks were saved there last session. A
     * file that can't be read at all isn't fatal — the session starts from
     * an empty list and run() warns about it — so that a single unreadable
     * file doesn't stop the user from using the chatbot at all.
     */
    public Myriad(String filePath) {
        this.ui = new Ui();
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
     * Greets the user, warns about anything that couldn't be loaded,
     * handles commands until the user exits, then says goodbye.
     */
    public void run() {
        ui.showGreeting();
        if (loadErrorMessage != null) {
            ui.showLoadingError(loadErrorMessage);
        }
        if (!skippedLines.isEmpty()) {
            ui.showLoadWarning(skippedLines);
        }
        readCommands();
        ui.showFarewell();
    }

    public static void main(String[] args) {
        // Built with File rather than a "data/myriad.txt" literal so the
        // separator is right on every OS.
        new Myriad(new File("data", "myriad.txt").getPath()).run();
    }

    /**
     * Takes lines from the Ui until the user enters the exit command
     * (matched case-insensitively, ignoring leading/trailing whitespace) or
     * the input runs out, then returns so the caller can print the
     * farewell. Each line is handed to the Parser, which returns the
     * Command it asks for; carrying that command out is the command's own
     * business, so this loop doesn't need to know which commands exist.
     * Both parsing and executing may throw MyriadException instead of
     * displaying an error themselves, so this is the single place that
     * catches it and shows it — with the "Error: " prefix added here
     * rather than repeated in every message. Commands also raise
     * MyriadException (rather than a checked IOException) when saving to
     * disk fails, so a save failure is caught and shown the same way as
     * any other command error, instead of crashing the program.
     */
    private void readCommands() {
        while (ui.hasNextCommand()) {
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
                    return;
                }
            } catch (MyriadException e) {
                ui.showError("Error: " + e.getMessage());
            }
        }
    }
}
