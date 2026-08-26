/**
 * Signals that a line of text couldn't be turned into a valid command or
 * task — either typed by the user (e.g. a missing task number, or a
 * missing task description/date), or read back from the saved data file
 * (e.g. a corrupted or unrecognized line). The message is the specific
 * reason only — callers displaying it to the user are responsible for
 * adding the "Error: " prefix, so the same message stays reusable
 * elsewhere (e.g. in a future log or test assertion) without carrying
 * that prefix.
 */
public class MyriadException extends Exception {
    public MyriadException(String message) {
        super(message);
    }
}
