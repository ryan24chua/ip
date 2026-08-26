import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Handles saving and loading task data on disk. Tasks are stored one per
 * line in a single file, using each task's save format (not its display
 * format) so a line can be parsed back into the right Task subclass.
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

    /**
     * Loads tasks from the data file into taskList, one per line, restoring
     * each task's type, description, type-specific fields, and done status
     * from its save format. Does nothing if the data file doesn't exist yet
     * (e.g. first run) rather than treating that as an error. Not yet
     * handled: a malformed line (wrong field count, unreadable file after
     * the exists() check) throws unchecked/checked exceptions that aren't
     * caught, since error handling is a later increment.
     */
    public static void readData(TaskList taskList) throws FileNotFoundException {
        File dataFile = new File(DATA_FILE);

        if (!dataFile.exists()) {
            return;
        }

        try (Scanner reader = new Scanner(dataFile)) {
            while (reader.hasNextLine()) {
                String[] p = reader.nextLine().split("\\s*\\|\\s*");
                String type = p[0];
                boolean isDone = p[1].equals("1");
                String description = p[2];

                Task task = switch (type) {
                    case "D" -> new Deadline(description, p[3]);
                    case "E" -> new Event(description, p[3], p[4]);
                    default -> new ToDo(description);
                };
                task.setDone(isDone);
                taskList.add(task);
            }
        }
    }
}
