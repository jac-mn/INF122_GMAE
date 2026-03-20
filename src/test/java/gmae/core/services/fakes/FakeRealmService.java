package gmae.core.services.fakes;

import gmae.core.api.PlayerId;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.RealmView;
import gmae.core.services.GmaeRealmService;

import java.util.*;

/**
 * Deterministic in-memory implementation of {@link GmaeRealmService}.
 *
 * <p>Two hardcoded realms: "Shadowfen" and "Crystalpeak".
 * Supports 10×10 grids with deterministic entity ids.</p>
 */
public class FakeRealmService implements GmaeRealmService {

    private static final int GRID_SIZE = 10;
    private static final int MAX_COORD = GRID_SIZE - 1;
    private static final Map<String, RealmView> REALMS = Map.of(
            "Shadowfen",   new RealmView("shadowfen", "Shadowfen", "A misty marsh realm"),
            "Crystalpeak", new RealmView("crystalpeak", "Crystalpeak", "A mountain realm")
    );

    private final Map<PlayerId, String> playerRealms = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Coord> playerPositions = new EnumMap<>(PlayerId.class);
    private final Map<String, EntityView> entities = new LinkedHashMap<>();
    private int nextEntityId = 1;

    @Override
    public List<RealmView> listRealms() {
        return List.copyOf(REALMS.values());
    }

    @Override
    public Optional<Coord> playerPosition(PlayerId player) {
        return Optional.ofNullable(playerPositions.get(player));
    }

    @Override
    public void placePlayer(PlayerId player, String realm, Coord position) {
        if (!REALMS.containsKey(realm)) {
            throw new IllegalArgumentException("Unknown realm: " + realm);
        }
        if (!isInBounds(position)) {
            throw new IllegalArgumentException("Position out of bounds: " + position);
        }
        playerRealms.put(player, realm);
        playerPositions.put(player, position);
    }

    @Override
    public boolean movePlayer(PlayerId player, int dx, int dy) {
        Coord current = playerPositions.get(player);
        if (current == null) {
            return false;
        }
        Coord newPos = new Coord(current.x() + dx, current.y() + dy);
        if (!isInBounds(newPos)) {
            return false;
        }
        playerPositions.put(player, newPos);
        return true;
    }

    @Override
    public List<EntityView> listEntitiesInRealm(String realm) {
        return entities.values().stream()
                .filter(e -> e.realm().equals(realm))
                .toList();
    }

    @Override
    public String addEntity(EntityView entity) {
        String id = entity.id();
        if (id == null || id.isEmpty()) {
            id = "entity-" + nextEntityId++;
        }
        EntityView normalized = new EntityView(id, entity.name(), entity.type(),
                entity.position(), entity.realm());
        entities.put(id, normalized);
        return id;
    }

    @Override
    public boolean removeEntity(String entityId) {
        return entities.remove(entityId) != null;
    }

    private boolean isInBounds(Coord c) {
        return c.x() >= 0 && c.x() <= MAX_COORD
            && c.y() >= 0 && c.y() <= MAX_COORD;
    }
}
