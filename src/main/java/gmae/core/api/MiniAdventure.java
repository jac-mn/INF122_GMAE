package gmae.core.api;

import gmae.core.model.AdventureState;
import gmae.core.services.ServiceBundle;

/**
 * The contract every mini-adventure must implement.
 *
 * <h2>Lifecycle</h2>
 * The engine drives a mini-adventure through these calls in order:
 *
 * <pre>
 *   1. init()                          — one-time setup
 *   2. bindServices(bundle)            — engine injects available services
 *   3. loop until isComplete():
 *        a. acceptInput(player, event) — 0-N times per step (one per player action)
 *        b. advance()                  — once per step (processes buffered input)
 *        c. reportState()              — once per step (UI reads the snapshot)
 *   4. reset()                         — if the players want to replay
 * </pre>
 *
 * <h2>Turn-based vs. tick-based</h2>
 * <ul>
 *   <li><strong>Turn-based:</strong> the engine calls {@code acceptInput} for
 *       the current player, then {@code advance()}, then checks state.</li>
 *   <li><strong>Tick-based:</strong> the engine calls {@code acceptInput} for
 *       both players (buffering), then {@code advance()} on a timer, then
 *       checks state.</li>
 * </ul>
 * The interface is identical in both modes — only the engine's call cadence
 * differs. Implementations must not assume either mode.
 *
 * <h2>Two-player model</h2>
 * Input is always tagged with a {@link PlayerId} ({@code P1} or {@code P2}).
 * {@link #reportState()} must return a {@link AdventureState} containing a
 * {@link gmae.core.model.PlayerView} for both players.
 *
 * <h2>Determinism</h2>
 * Given the same sequence of {@code acceptInput} calls, two runs of
 * {@code advance()} should produce the same state. Adventures that need
 * randomness should seed a local RNG in {@code init()} so replays are
 * possible in the future.
 *
 * <h2>Stability promise</h2>
 * <strong>This interface is frozen after Day 2.</strong> After the freeze:
 * <ul>
 *   <li>New methods may only be added with {@code default} implementations.</li>
 *   <li>Existing method signatures will not change.</li>
 *   <li>Breaking changes require a full team sync and a documented reason.</li>
 * </ul>
 */
public interface MiniAdventure {

    /**
     * Stable, unique identifier used by the engine registry and persistence.
     *
     * <p>Must be a non-null, non-empty string that never changes across
     * versions (e.g. {@code "treasure-hunt"}, {@code "arena-duel"}).
     * Use lowercase-kebab-case by convention.</p>
     *
     * @return non-null unique adventure identifier
     */
    String id();

    /**
     * Human-readable title shown in the adventure selection menu.
     *
     * @return non-null display name (e.g. "Treasure Hunt", "Arena Duel")
     */
    String title();

    /**
     * Short description shown below the title in the adventure menu.
     *
     * @return non-null one-line summary (e.g. "Race to collect the most
     *         treasure before time runs out!")
     */
    String description();

    /**
     * Initialises the adventure to its starting state. Called exactly once
     * before the first {@code acceptInput}/{@code advance} cycle.
     *
     * <p>Implementations should allocate internal structures, set up the
     * board/map, and prepare both players here.</p>
     */
    void init();

    /**
     * Called by the engine after {@link #init()} to inject available services.
     *
     * <p>Adventures that need time, inventory, or quest-event services should
     * store the bundle and retrieve individual services via
     * {@link ServiceBundle#timeService()}, etc.  Adventures that need no
     * services can ignore this method (the default is a no-op).</p>
     *
     * <p><strong>Backward-compatible addition (default method).</strong>
     * Existing adventures that do not override this will compile and run
     * unchanged.</p>
     *
     * @param services the service bundle (never {@code null})
     */
    default void bindServices(ServiceBundle services) {
        // no-op — adventures opt in by overriding
    }

    /**
     * Buffers one player action to be processed on the next {@link #advance}.
     *
     * <p>The engine guarantees {@code player} is either {@code P1} or
     * {@code P2}. In turn-based mode only the active player's input is
     * forwarded; in tick-based mode both players may submit input before
     * each advance.</p>
     *
     * @param player who submitted the action
     * @param event  the action (never {@code null})
     */
    void acceptInput(PlayerId player, InputEvent event);

    /**
     * Advances the simulation by one step (one turn or one tick).
     *
     * <p>Processes all input buffered since the last advance, updates
     * internal state, and clears the input buffer. Must be safe to call
     * even when no input was received (a "no-op turn").</p>
     */
    void advance();

    /**
     * Returns an immutable snapshot of the current adventure state.
     *
     * <p>The returned {@link AdventureState} must contain a
     * {@link gmae.core.model.PlayerView} for both {@code P1} and
     * {@code P2}, an accurate {@code tickOrTurn} counter, any new
     * messages, and the current {@link gmae.core.model.Outcome}.</p>
     *
     * @return non-null state snapshot; a new instance on every call
     */
    AdventureState reportState();

    /**
     * Returns {@code true} when the adventure has reached a terminal state
     * (win, loss, draw). Once this returns {@code true}, the engine will
     * stop calling {@code acceptInput}/{@code advance}.
     */
    boolean isComplete();

    /**
     * Resets the adventure to the same state as immediately after
     * {@link #init()}. This allows replaying without re-constructing
     * the object.
     */
    void reset();
}
