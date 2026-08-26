public class ToDo extends Task {

    public ToDo(String description) {
        super(description);
    }

    /**
     * Returns this task's save-format line prefixed with "T" so it can be
     * recognized as a ToDo when the data file is read back in.
     */
    @Override
    public String toSaveFormat() {
        return String.format("T | %s", super.toSaveFormat());
    }

    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }
}
