package gmae.core.model;

/**
 * Describes the outcome of a mini-adventure.
 *
 * <p>Used by {@link AdventureState} to express both competitive and
 * cooperative results through a single enum.</p>
 *
 * <ul>
 *   <li>{@link #IN_PROGRESS} — adventure is still running</li>
 *   <li>{@link #P1_WINS}     — player 1 wins (competitive)</li>
 *   <li>{@link #P2_WINS}     — player 2 wins (competitive)</li>
 *   <li>{@link #DRAW}        — competitive tie</li>
 *   <li>{@link #COOP_WIN}    — both players win together (cooperative)</li>
 *   <li>{@link #LOSS}        — both players lose (cooperative)</li>
 * </ul>
 */
public enum Outcome {
    IN_PROGRESS,
    P1_WINS,
    P2_WINS,
    DRAW,
    COOP_WIN,
    LOSS
}
