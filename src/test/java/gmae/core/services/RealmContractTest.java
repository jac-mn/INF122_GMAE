package gmae.core.services;

import gmae.core.api.PlayerId;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.RealmView;
import gmae.core.services.fakes.FakeRealmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link GmaeRealmService}.
 *
 * <p>Run against the {@link FakeRealmService} reference implementation.
 * Sub-team #2 can copy this class, swap the implementation in
 * {@link #createService()}, and verify their adapter passes the same
 * invariants.</p>
 */
public class RealmContractTest {

    private GmaeRealmService svc;

    /** Override this method to test a different implementation. */
    protected GmaeRealmService createService() {
        return new FakeRealmService();
    }

    @BeforeEach
    void setUp() {
        svc = createService();
    }

    // ── realms ─────────────────────────────────────────────

    @Test
    void listRealms_returnsAtLeastTwoRealms() {
        List<RealmView> realms = svc.listRealms();
        assertTrue(realms.size() >= 2, "Should have at least 2 realms");
    }

    // ── player placement ───────────────────────────────────

    @Test
    void playerPosition_emptyByDefault() {
        assertTrue(svc.playerPosition(PlayerId.P1).isEmpty());
    }

    @Test
    void placePlayer_storesPosition() {
        svc.placePlayer(PlayerId.P1, "Shadowfen", new Coord(3, 5));
        Coord pos = svc.playerPosition(PlayerId.P1).orElse(null);
        assertNotNull(pos);
        assertEquals(3, pos.x());
        assertEquals(5, pos.y());
    }

    @Test
    void placePlayer_playerIsolation() {
        svc.placePlayer(PlayerId.P1, "Shadowfen", new Coord(1, 1));
        svc.placePlayer(PlayerId.P2, "Shadowfen", new Coord(8, 8));
        assertEquals(new Coord(1, 1), svc.playerPosition(PlayerId.P1).orElse(null));
        assertEquals(new Coord(8, 8), svc.playerPosition(PlayerId.P2).orElse(null));
    }

    // ── movement ───────────────────────────────────────────

    @Test
    void movePlayer_success() {
        svc.placePlayer(PlayerId.P1, "Shadowfen", new Coord(5, 5));
        assertTrue(svc.movePlayer(PlayerId.P1, 1, 0));
        assertEquals(new Coord(6, 5), svc.playerPosition(PlayerId.P1).orElse(null));
    }

    @Test
    void movePlayer_outOfBounds_fails() {
        svc.placePlayer(PlayerId.P1, "Shadowfen", new Coord(9, 9));
        assertFalse(svc.movePlayer(PlayerId.P1, 1, 0), "Should fail at boundary");
        assertEquals(new Coord(9, 9), svc.playerPosition(PlayerId.P1).orElse(null));
    }

    @Test
    void movePlayer_notPlaced_fails() {
        assertFalse(svc.movePlayer(PlayerId.P1, 1, 0), "Cannot move player not placed");
    }

    // ── entities ───────────────────────────────────────────

    @Test
    void listEntitiesInRealm_emptyByDefault() {
        assertTrue(svc.listEntitiesInRealm("Shadowfen").isEmpty());
    }

    @Test
    void addEntity_returnsId() {
        EntityView ent = new EntityView("", "Goblin", "npc",
                new Coord(2, 2), "Shadowfen");
        String id = svc.addEntity(ent);
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void listEntitiesInRealm_containsAddedEntity() {
        EntityView ent = new EntityView("treasure-1", "Gold", "treasure",
                new Coord(4, 4), "Shadowfen");
        svc.addEntity(ent);
        List<EntityView> list = svc.listEntitiesInRealm("Shadowfen");
        assertEquals(1, list.size());
        assertEquals("treasure-1", list.get(0).id());
    }

    @Test
    void removeEntity_success() {
        EntityView ent = new EntityView("obs-1", "Rock", "obstacle",
                new Coord(1, 1), "Shadowfen");
        String id = svc.addEntity(ent);
        assertTrue(svc.removeEntity(id));
        assertTrue(svc.listEntitiesInRealm("Shadowfen").isEmpty());
    }

    @Test
    void removeEntity_nonexistent_fails() {
        assertFalse(svc.removeEntity("nonexistent"));
    }
}
