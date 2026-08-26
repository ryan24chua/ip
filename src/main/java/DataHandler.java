import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles saving and loading task data on disk. Tasks are stored one per
 * line in a single file, using each task's save format (not its display
 * format) so a line can be parsed back into the right Task subclass.
 */
public class DataHandler {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = new File(DATA_DIR, "myriad.txt").getPath();

    /**
     * Overwrites the data file with every task currently in taskList, one
     * save-format line each, creating the data directory first if it
     * doesn't already exist (e.g. on a fresh checkout). Called after every
     * mutating command (add/mark/unmark/delete) so the file always exactly
     * mirrors the in-memory list — simpler than tracking per-task deltas,
     * and cheap at the scale of a personal task list. IOException (e.g.
     * disk full, permission denied) is declared for the caller to
     * translate into a user-facing message — see Myriad.save.
     */
    public static void saveAll(TaskList taskList) throws IOException {
        new File(DATA_DIR).mkdirs();

        try (FileWriter writer = new FileWriter(DATA_FILE, false)) {
            for (Task task : taskList.asList()) {
                writer.write(task.toSaveFormat() + "\n");
            }
        }
    }

    /**
     * Parses one saved-data line back into a Task, restoring its type,
     * description, type-specific fields, and done status from the "|"
     * -delimited save format written by Task.toSaveFormat(). Throws
     * MyriadException if the line has too few fields for its type, or an
     * unrecognized leading type letter — deliberately, rather than
     * guessing (e.g. silently treating an unknown type as a ToDo), so the
     * caller can report and skip it instead of loading a wrong task.
     */
    private static Task parseLine(String line) throws MyriadException {
        String[] p = line.split("\\s*\\|\\s*");
        if (p.length < 3) {
            throw new MyriadException(
                    "expected at least 3 fields (type | done | description), found " + p.length);
        }
        String type = p[0];
        boolean isDone = p[1].equals("1");
        String description = p[2];

        Task task = switch (type) {
            case "T" -> new ToDo(description);
            case "D" -> {
                if (p.length < 4) {
                    throw new MyriadException(
                            "a Deadline line needs a 4th field (date), found " + p.length + " fields");
                }
                yield new Deadline(description, p[3]);
            }
            case "E" -> {
                if (p.length < 5) {
                    throw new MyriadException(
                            "an Event line needs 5 fields (type, done, description, start, end), "
                                    + "found " + p.length);
                }
                yield new Event(description, p[3], p[4]);
            }
            default -> throw new MyriadException("unknown task type \"" + type + "\"");
        };
        task.setDone(isDone);
        return task;
    }

    /**
     * Loads tasks from the data file into taskList, one per line, via
     * parseLine. Does nothing if the data file doesn't exist yet (e.g.
     * first run) rather than treating that as an error. A line that
     * parseLine rejects is skipped — not fatal, and not silently
     * misinterpreted — and its 1-based line number and reason are added to
     * the returned list so the caller can show one consolidated warning;
     * every other, valid line still loads normally.
     */
    public static List<String> readData(TaskList taskList) throws FileNotFoundException {
        List<String> skippedLines = new ArrayList<>();
        File dataFile = new File(DATA_FILE);

        if (!dataFile.exists()) {
            return skippedLines;
        }

        try (Scanner reader = new Scanner(dataFile)) {
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                lineNumber++;
                String line = reader.nextLine();
                try {
                    taskList.add(parseLine(line));
                } catch (MyriadException e) {
                    skippedLines.add("line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return skippedLines;
    }
}
