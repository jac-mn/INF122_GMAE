package gmae.core.model;

import gmae.core.api.PlayerId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, serializable-friendly snapshot of one player's visible state
 * inside a running mini-adventure.
 *
 * <p>The {@code attributes} map is an open extension point: adventures can
 * expose arbitrary key-value pairs (e.g. {@code "hp" → "42"},
 * {@code "position" → "3,7"}) without changing the frozen API surface.</p>
 */
public final class PlayerView {

    private final PlayerId id;
    private final String   name;
    private final int      score;
    private final Map<String, String> attributes;

    /**
     * @param id         which player this view represents
     * @param name       display name
     * @param score      current score (adventure-defined semantics)
     * @param attributes adventure-specific key-value data (defensively copied)
     */
    public PlayerView(PlayerId id, String name, int score,
                      Map<String, String> attributes) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.score = score;
        this.attributes = attributes == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /** Convenience constructor with no extra attributes. */
    public PlayerView(PlayerId id, String name, int score) {
        this(id, name, score, null);
    }

    public PlayerId id()                      { return id; }
    public String   name()                    { return name; }
    public int      score()                   { return score; }
    public Map<String, String> attributes()   { return attributes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerView that)) return false;
        return score == that.score
                && id == that.id
                && name.equals(that.name)
                && attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, score, attributes);
    }

    @Override
    public String toString() {
        return "PlayerView[" + id + ", " + name + ", score=" + score
                + ", attrs=" + attributes + "]";
    }
}
