package gmae.core.engine;

import gmae.core.api.MiniAdventure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Stores adventure factories keyed by id and exposes metadata for menu display.
 *
 * <p>Each registered factory is probed once at registration time to capture
 * {@code id()}, {@code title()}, and {@code description()}.  Subsequent calls
 * to {@link #create(String)} invoke the factory for a fresh instance.</p>
 */
public class AdventureRegistry {

    /** Read-only metadata snapshot used for menu display. */
    public record EntryInfo(String id, String title, String description) {}

    private final Map<String, EntryInfo> metadata = new LinkedHashMap<>();
    private final Map<String, Supplier<MiniAdventure>> factories = new LinkedHashMap<>();

    /**
     * Registers an adventure factory.  A probe instance is created once to
     * read {@code id()}, {@code title()}, and {@code description()}, then
     * discarded.
     */
    public void register(Supplier<MiniAdventure> factory) {
        MiniAdventure probe = factory.get();
        String id = probe.id();
        metadata.put(id, new EntryInfo(id, probe.title(), probe.description()));
        factories.put(id, factory);
    }

    /** Returns metadata for every registered adventure, in registration order. */
    public List<EntryInfo> listAdventures() {
        return List.copyOf(metadata.values());
    }

    /** Creates a fresh adventure instance by id. */
    public MiniAdventure create(String id) {
        Supplier<MiniAdventure> factory = factories.get(id);
        if (factory == null) {
            throw new NoSuchElementException("No adventure registered with id: " + id);
        }
        return factory.get();
    }

    public boolean has(String id) {
        return factories.containsKey(id);
    }
}
