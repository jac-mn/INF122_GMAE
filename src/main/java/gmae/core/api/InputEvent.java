package gmae.core.api;

import java.util.Objects;

/**
 * A generic, serializable-friendly input action submitted by a player.
 *
 * <p>{@code action} is a non-null string identifying the action type
 * (e.g. {@code "MOVE"}, {@code "USE_ITEM"}, {@code "PASS"}).
 * {@code payload} carries optional context (e.g. {@code "NORTH"},
 * an item name, or {@code null} when the action needs no extra data).</p>
 *
 * <p>Adventure implementations define which action strings they recognise;
 * unrecognised actions should be silently ignored or produce a message
 * in the next {@link gmae.core.model.AdventureState}.</p>
 *
 * <p><strong>Frozen contract:</strong> the fields and constructor signature
 * of this class will not change after the API freeze. New convenience
 * factory methods may be added.</p>
 */
public final class InputEvent {

    private final String action;
    private final String payload;

    /**
     * Creates an input event.
     *
     * @param action  non-null action identifier
     * @param payload optional context data (may be {@code null})
     */
    public InputEvent(String action, String payload) {
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.payload = payload;
    }

    /** Convenience factory for actions that carry no payload. */
    public static InputEvent of(String action) {
        return new InputEvent(action, null);
    }

    /** Convenience factory for actions with a payload. */
    public static InputEvent of(String action, String payload) {
        return new InputEvent(action, payload);
    }

    public String action()  { return action; }
    public String payload() { return payload; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InputEvent that)) return false;
        return action.equals(that.action) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, payload);
    }

    @Override
    public String toString() {
        return payload == null
                ? "InputEvent[" + action + "]"
                : "InputEvent[" + action + ", " + payload + "]";
    }
}
