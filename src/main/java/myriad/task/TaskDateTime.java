package myriad.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import myriad.MyriadException;

/**
 * An immutable date, optionally paired with a time, parsed from user- or
 * save-file-supplied text. Whether a time was given matters: it decides
 * both what toString() prints (a bare date vs. a date+time) and lets
 * "no time" be represented honestly instead of being defaulted to
 * midnight and displayed as if the user had typed one.
 * <p>
 * The only way to build one is parse(String) — it tries a fixed
 * list of formats in turn, so callers never need to know which specific
 * format the text ended up matching.
 */
public class TaskDateTime {
    /**
     * Formats that include a time component, tried before the date-only
     * ones below. Trying these first is what keeps parsing unambiguous:
     * a date-only string like "2019-12-02" simply doesn't have enough
     * characters to satisfy any of these, so it always falls through to
     * the date-only attempts rather than being mis-parsed here.
     */
    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("d/M/yyyy HHmm"),
            DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"));

    /**
     * Date-only formats, tried after every date+time format has failed.
     */
    private static final List<DateTimeFormatter> DATE_ONLY_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/yyyy"));

    /** Display format used by toString() when a time is present. */
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("MMM dd yyyy HHmm");

    /** Display format used by toString() when only a date is present. */
    private static final DateTimeFormatter DISPLAY_DATE_ONLY =
            DateTimeFormatter.ofPattern("MMM dd yyyy");

    private final LocalDate date;
    private final LocalTime time;

    private TaskDateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Parses raw against, in order, every format in DATE_TIME_FORMATS then
     * every format in DATE_ONLY_FORMATS, returning as soon as one succeeds.
     * Throws MyriadException, naming a couple of example accepted formats,
     * if raw matches none of them.
     */
    public static TaskDateTime parse(String raw) throws MyriadException {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(raw, formatter);
                return new TaskDateTime(dateTime.toLocalDate(), dateTime.toLocalTime());
            } catch (DateTimeParseException e) {
                // Try the next format.
            }
        }
        for (DateTimeFormatter formatter : DATE_ONLY_FORMATS) {
            try {
                return new TaskDateTime(LocalDate.parse(raw, formatter), null);
            } catch (DateTimeParseException e) {
                // Try the next format.
            }
        }
        throw new MyriadException(
                "\"" + raw + "\" doesn't look like a date/time I understand. Try formats "
                        + "like \"2019-12-02\" or \"2019-12-02 1800\" (or \"2/12/2019\" / "
                        + "\"2/12/2019 1800\").");
    }

    /**
     * Returns this value as an ISO-8601 string suitable for saving to disk:
     * date+time (with the "T" separator) if a time is present, otherwise
     * just the date. Both forms are themselves accepted by parse() (they're
     * DATE_TIME_FORMATS.get(0) and DATE_ONLY_FORMATS.get(0) respectively),
     * so a value written by toSaveFormat() is always re-readable by parse()
     * on the next load.
     */
    public String toSaveFormat() {
        return time == null ? date.toString() : LocalDateTime.of(date, time).toString();
    }

    /**
     * Returns the earliest instant this value could refer to: the exact
     * date+time if a time is present, otherwise the very start of date
     * (00:00). Paired with rangeEnd(), this lets a date-only value stand
     * in for "any time during that day" when checking overlap with
     * another TaskDateTime.
     */
    public LocalDateTime rangeStart() {
        return time == null ? LocalDateTime.of(date, LocalTime.MIN) : LocalDateTime.of(date, time);
    }

    /**
     * Returns the latest instant this value could refer to: the exact
     * date+time if a time is present, otherwise the very end of date
     * (23:59:59.999999999). See rangeStart().
     */
    public LocalDateTime rangeEnd() {
        return time == null ? LocalDateTime.of(date, LocalTime.MAX) : LocalDateTime.of(date, time);
    }

    /**
     * Returns whether the closed interval [aStart, aEnd] overlaps the
     * closed interval [bStart, bEnd]. Shared by Deadline and Event's
     * occursDuring(TaskDateTime) — each just supplies its own two range
     * endpoints (a Deadline's range is its own rangeStart()/rangeEnd(); an
     * Event's spans from its start's rangeStart() to its end's rangeEnd()).
     */
    public static boolean rangesOverlap(
            LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    /**
     * Returns this value in the user-facing display format: "MMM dd yyyy"
     * for a date-only value, or "MMM dd yyyy HHmm" when a time is present.
     */
    @Override
    public String toString() {
        return time == null
                ? date.format(DISPLAY_DATE_ONLY)
                : LocalDateTime.of(date, time).format(DISPLAY_DATE_TIME);
    }
}
