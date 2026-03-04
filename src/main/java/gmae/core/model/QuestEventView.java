package gmae.core.model;

import java.util.Objects;

/**
 * Immutable snapshot of a quest event — suitable for display or
 * adventure logic without exposing legacy domain types.
 */
public final class QuestEventView {

    private final String id;
    private final String title;
    private final long   startMinutes;
    private final long   endMinutes;
    private final String realmName;

    /**
     * @param id           event identifier (UUID string)
     * @param title        event title
     * @param startMinutes start time in total world-minutes
     * @param endMinutes   end time in total world-minutes ({@code -1} if open-ended)
     * @param realmName    display name of the associated realm
     */
    public QuestEventView(String id, String title, long startMinutes,
                          long endMinutes, String realmName) {
        this.id           = Objects.requireNonNull(id);
        this.title        = Objects.requireNonNull(title);
        this.startMinutes = startMinutes;
        this.endMinutes   = endMinutes;
        this.realmName    = realmName == null ? "" : realmName;
    }

    public String id()           { return id; }
    public String title()        { return title; }
    public long   startMinutes() { return startMinutes; }
    public long   endMinutes()   { return endMinutes; }
    public String realmName()    { return realmName; }
    public boolean isOpenEnded() { return endMinutes < 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestEventView that)) return false;
        return startMinutes == that.startMinutes && endMinutes == that.endMinutes
                && id.equals(that.id) && title.equals(that.title)
                && realmName.equals(that.realmName);
    }

    @Override public int hashCode() { return Objects.hash(id, title, startMinutes, endMinutes, realmName); }

    @Override
    public String toString() {
        return "QuestEventView[" + title + " @ " + realmName + "]";
    }
}
