import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles saving task data to disk. Tasks are appended to a single file,
 * one per line, using each task's save format (not its display format) so
 * the line can later be parsed back into a Task.
 */
public class DataHandler {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = DATA_DIR + "/myriad.txt";

    /**
     * Appends the given task's save format to the data file, creating the
     * data directory first if it doesn't already exist (e.g. on a fresh
     * checkout). Not yet handled: IOException is declared and left to
     * propagate, since error handling is a later increment.
     */
    public static void writeData(Task task) throws IOException {
        new File(DATA_DIR).mkdirs();

        try (FileWriter writer = new FileWriter(DATA_FILE, true)) {
            writer.write(task.toSaveFormat() + "\n");
        }
    }
}
