package gmae.core.model;

import java.util.Objects;

/**
 * Immutable snapshot of a point in time.
 *
 * <p>{@code days}, {@code hours}, {@code minutes} express world time.
 * {@code localTimeString} is an optional pre-formatted realm-local time
 * (null when no realm context is available).</p>
 */
public final class TimeView {

    private final int    days;
    private final int    hours;
    private final int    minutes;
    private final String localTimeString;

    public TimeView(int days, int hours, int minutes, String localTimeString) {
        this.days            = days;
        this.hours           = hours;
        this.minutes         = minutes;
        this.localTimeString = localTimeString;
    }

    /** Convenience constructor for world-time-only snapshots. */
    public TimeView(int days, int hours, int minutes) {
        this(days, hours, minutes, null);
    }

    public int    days()            { return days; }
    public int    hours()           { return hours; }
    public int    minutes()         { return minutes; }
    public String localTimeString() { return localTimeString; }

    /** Total elapsed minutes (useful for arithmetic). */
    public long toTotalMinutes() {
        return (long) days * 24 * 60 + (long) hours * 60 + minutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeView that)) return false;
        return days == that.days && hours == that.hours && minutes == that.minutes
                && Objects.equals(localTimeString, that.localTimeString);
    }

    @Override public int hashCode() { return Objects.hash(days, hours, minutes, localTimeString); }

    @Override
    public String toString() {
        String world = String.format("Day %d %02d:%02d", days, hours, minutes);
        return localTimeString == null ? world : world + " (local: " + localTimeString + ")";
    }
}
