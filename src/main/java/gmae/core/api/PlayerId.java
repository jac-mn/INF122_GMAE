package gmae.core.api;

/**
 * Identifies one of the two local players in a GMAE session.
 *
 * <p>Every mini-adventure receives input tagged with a {@code PlayerId}
 * and must report per-player state keyed by the same values.</p>
 */
public enum PlayerId {
    P1,
    P2
}
