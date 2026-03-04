package gmae.core.services;

import gmae.core.model.QuestEventView;
import gmae.core.services.fakes.FakeQuestEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link GmaeQuestEventService}.
 *
 * <p>Run against the {@link FakeQuestEventService} reference implementation.
 * Sub-team #2 can copy this class, swap the implementation in
 * {@link #createService()}, and verify their adapter passes the same
 * invariants.</p>
 */
public class QuestEventContractTest {

    private GmaeQuestEventService svc;

    /** Override this method to test a different implementation. */
    protected GmaeQuestEventService createService() {
        return new FakeQuestEventService();
    }

    @BeforeEach
    void setUp() {
        svc = createService();
    }

    // ── add + list ─────────────────────────────────────────────

    @Test
    void addEvent_returnsStableId() {
        String id = svc.addEvent(event("Goblin Raid", 100, 200, "Shadowfen"));

        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void listEvents_containsAddedEvent() {
        String id = svc.addEvent(event("Goblin Raid", 100, 200, "Shadowfen"));

        List<QuestEventView> events = svc.listEvents();
        assertEquals(1, events.size());
        assertEquals(id, events.get(0).id());
        assertEquals("Goblin Raid", events.get(0).title());
        assertEquals(100, events.get(0).startMinutes());
        assertEquals(200, events.get(0).endMinutes());
        assertEquals("Shadowfen", events.get(0).realmName());
    }

    @Test
    void addMultipleEvents_allPresent() {
        svc.addEvent(event("A", 0, 60, "R1"));
        svc.addEvent(event("B", 60, 120, "R2"));

        assertEquals(2, svc.listEvents().size());
    }

    // ── remove ─────────────────────────────────────────────────

    @Test
    void removeEvent_existing_returnsTrueAndRemoves() {
        String id = svc.addEvent(event("Raid", 0, 60, "Shadowfen"));

        assertTrue(svc.removeEvent(id));
        assertTrue(svc.listEvents().isEmpty());
    }

    @Test
    void removeEvent_nonexistent_returnsFalse() {
        assertFalse(svc.removeEvent("does-not-exist"));
    }

    @Test
    void removeEvent_doesNotAffectOtherEvents() {
        String id1 = svc.addEvent(event("A", 0, 60, "R1"));
        String id2 = svc.addEvent(event("B", 60, 120, "R2"));

        assertTrue(svc.removeEvent(id1));

        List<QuestEventView> remaining = svc.listEvents();
        assertEquals(1, remaining.size());
        assertEquals(id2, remaining.get(0).id());
    }

    // ── empty state ────────────────────────────────────────────

    @Test
    void listEvents_emptyByDefault() {
        assertTrue(svc.listEvents().isEmpty());
    }

    // ── helpers ────────────────────────────────────────────────

    private static QuestEventView event(String title, long start, long end, String realm) {
        return new QuestEventView("ignored", title, start, end, realm);
    }
}
