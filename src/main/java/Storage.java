import myriad.MyriadException;
import myriad.task.Deadline;
import myriad.task.Event;
import myriad.task.Task;
import myriad.task.TaskDateTime;
import myriad.task.ToDo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles saving and loading task data on disk. Tasks are stored one per
 * line in a single file, using each task's save format (not its display
 * format) so a line can be parsed back into the right Task subclass.
 *
 * The file to use is given to the constructor rather than fixed as a
 * constant, so which file this reads and writes is decided in one place
 * (Myriad's main method) instead of being hidden in here, and a test or a
 * second chatbot instance can point at a file of its own.
 */
public class Storage {

    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Overwrites the data file with every task currently in taskList, one
     * save-format line each, creating the parent directory first if it
     * doesn't already exist (e.g. on a fresh checkout). Called after every
     * mutating command (add/mark/unmark/delete) so the file always exactly
     * mirrors the in-memory list — simpler than tracking per-task deltas,
     * and cheap at the scale of a personal task list. IOException (e.g.
     * disk full, permission denied) is declared for the caller to
     * translate into a user-facing message — see Myriad.save.
     */
    public void save(TaskList taskList) throws IOException {
        File parentDir = new File(filePath).getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(filePath, false)) {
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
     *
     * This stays here rather than moving to Parser: it reads the save
     * format this class itself writes, which is a detail of how tasks are
     * stored, not of the command language the user types.
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
                yield new Deadline(description, TaskDateTime.parse(p[3]));
            }
            case "E" -> {
                if (p.length < 5) {
                    throw new MyriadException(
                            "an Event line needs 5 fields (type, done, description, start, end), "
                                    + "found " + p.length);
                }
                yield new Event(description, TaskDateTime.parse(p[3]), TaskDateTime.parse(p[4]));
            }
            default -> throw new MyriadException("unknown task type \"" + type + "\"");
        };
        task.setDone(isDone);
        return task;
    }

    /**
     * Reads the data file and returns the tasks it holds, one per line, via
     * parseLine. Returns an empty result if the data file doesn't exist yet
     * (e.g. first run) rather than treating that as an error. A line that
     * parseLine rejects is skipped — not fatal, and not silently
     * misinterpreted — and its 1-based line number and reason are collected
     * into the result's skippedLines so the caller can show one consolidated
     * warning; every other, valid line still loads normally.
     *
     * Returns the tasks instead of adding them to a TaskList passed in, so
     * the caller decides what to do with them (see Myriad's constructor,
     * which either builds a TaskList from them or falls back to an empty
     * one). Throws MyriadException if the file exists but can't be opened at
     * all — permission denied, or the path is a directory — which is a real
     * failure worth reporting, unlike the file simply not being there yet.
     */
    public LoadResult load() throws MyriadException {
        List<Task> tasks = new ArrayList<>();
        List<String> skippedLines = new ArrayList<>();
        File dataFile = new File(filePath);

        if (!dataFile.exists()) {
            return new LoadResult(tasks, skippedLines);
        }

        try (Scanner reader = new Scanner(dataFile)) {
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                lineNumber++;
                String line = reader.nextLine();
                try {
                    tasks.add(parseLine(line));
                } catch (MyriadException e) {
                    skippedLines.add("line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            throw new MyriadException(e.getMessage());
        }
        return new LoadResult(tasks, skippedLines);
    }
}
