/**
 * Recognized user commands. A line that isn't a recognized keyword maps
 * to UNKNOWN, which throws a MyriadException instead of doing anything.
 *
 * Named CommandType rather than Command because it says only which
 * command was typed, not what to do about it — the doing still lives in
 * Myriad's handlers.
 */
public enum CommandType {
    UNKNOWN, LIST, EXIT, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, SHOW;
}
