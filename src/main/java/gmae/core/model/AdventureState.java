package gmae.core.model;

import gmae.core.api.PlayerId;

import java.util.Collections;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, serializable-friendly snapshot of an entire mini-adventure's
 * visible state at a single point in time.
 *
 * <p>This is what {@link gmae.core.api.MiniAdventure#reportState()} returns.
 * The UI layer renders it; the engine never mutates it. Adventure
 * implementations create a new instance on every call.</p>
 *
 * <h3>Field semantics</h3>
 * <ul>
 *   <li>{@code title}      — human-readable adventure name</li>
 *   <li>{@code tickOrTurn}  — monotonically increasing counter
 *       (turn number in turn-based, tick count in tick-based)</li>
 *   <li>{@code messages}    — ordered log of events since the last snapshot
 *       (e.g. "P1 moved north", "Treasure found!")</li>
 *   <li>{@code players}     — per-player view keyed by {@link PlayerId}</li>
 *   <li>{@code complete}    — {@code true} when the adventure has ended</li>
 *   <li>{@code outcome}     — current result; {@link Outcome#IN_PROGRESS}
 *       while {@code complete} is {@code false}</li>
 * </ul>
 */
public final class AdventureState {

    private final String title;
    private final int    tickOrTurn;
    private final List<String> messages;
    private final Map<PlayerId, PlayerView> players;
    private final boolean complete;
    private final Outcome outcome;

    private AdventureState(Builder b) {
        this.title      = Objects.requireNonNull(b.title,   "title required");
        this.tickOrTurn = b.tickOrTurn;
        this.messages   = b.messages == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(b.messages));
        this.players    = b.players == null || b.players.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new EnumMap<>(b.players));
        this.complete   = b.complete;
        this.outcome    = b.outcome == null ? Outcome.IN_PROGRESS : b.outcome;
    }

    // ── accessors ──────────────────────────────────────────────────

    public String  title()       { return title; }
    public int     tickOrTurn()  { return tickOrTurn; }
    public List<String> messages()             { return messages; }
    public Map<PlayerId, PlayerView> players() { return players; }
    public boolean complete()    { return complete; }
    public Outcome outcome()     { return outcome; }

    // ── builder ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String title;
        private int    tickOrTurn;
        private List<String> messages;
        private Map<PlayerId, PlayerView> players;
        private boolean complete;
        private Outcome outcome;

        private Builder() {}

        public Builder title(String v)                          { title = v;       return this; }
        public Builder tickOrTurn(int v)                        { tickOrTurn = v;  return this; }
        public Builder messages(List<String> v)                 { messages = v;    return this; }
        public Builder players(Map<PlayerId, PlayerView> v)     { players = v;     return this; }
        public Builder complete(boolean v)                      { complete = v;    return this; }
        public Builder outcome(Outcome v)                       { outcome = v;     return this; }

        public AdventureState build() { return new AdventureState(this); }
    }

    // ── Object overrides ───────────────────────────────────────────

    @Override
    public String toString() {
        return "AdventureState[" + title
                + ", turn=" + tickOrTurn
                + ", outcome=" + outcome
                + ", messages=" + messages.size()
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdventureState that)) return false;
        return tickOrTurn == that.tickOrTurn
                && complete == that.complete
                && title.equals(that.title)
                && messages.equals(that.messages)
                && players.equals(that.players)
                && outcome == that.outcome;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, tickOrTurn, messages, players, complete, outcome);
    }
}
