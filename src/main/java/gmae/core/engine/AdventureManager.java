package gmae.core.engine;

import gmae.core.api.InputEvent;
import gmae.core.api.MiniAdventure;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.services.ServiceBundle;

/**
 * Manages the lifecycle of a single running adventure: create → init →
 * bindServices → (acceptInput / advance / reportState) loop → reset.
 */
public class AdventureManager {

    private final AdventureRegistry registry;
    private final ServiceBundle services;
    private MiniAdventure current;

    public AdventureManager(AdventureRegistry registry, ServiceBundle services) {
        this.registry = registry;
        this.services = services;
    }

    public AdventureManager(AdventureRegistry registry) {
        this(registry, ServiceBundle.empty());
    }

    /**
     * Creates a new adventure from the registry, calls {@code init()}
     * then {@code bindServices()}, and returns the initial state.
     */
    public AdventureState start(String adventureId) {
        current = registry.create(adventureId);
        current.init();
        current.bindServices(services);
        return current.reportState();
    }

    /** Buffers one player's input for the next advance. */
    public void submitInput(PlayerId player, InputEvent event) {
        requireRunning();
        current.acceptInput(player, event);
    }

    /** Calls {@code advance()} and returns the resulting state snapshot. */
    public AdventureState step() {
        requireRunning();
        current.advance();
        return current.reportState();
    }

    public boolean isFinished() {
        return current != null && current.isComplete();
    }

    /** Calls {@code reset()} on the current adventure and returns the fresh initial state. */
    public AdventureState restart() {
        requireRunning();
        current.reset();
        return current.reportState();
    }

    public AdventureState currentState() {
        requireRunning();
        return current.reportState();
    }

    private void requireRunning() {
        if (current == null) {
            throw new IllegalStateException("No adventure is running");
        }
    }
}
