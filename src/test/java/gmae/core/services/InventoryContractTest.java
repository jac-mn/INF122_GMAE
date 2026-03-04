package gmae.core.services;

import gmae.core.api.PlayerId;
import gmae.core.model.ItemView;
import gmae.core.services.fakes.FakeInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link GmaeInventoryService}.
 *
 * <p>Run against the {@link FakeInventoryService} reference implementation.
 * Sub-team #2 can copy this class, swap the implementation in
 * {@link #createService()}, and verify their adapter passes the same
 * invariants.</p>
 */
public class InventoryContractTest {

    private GmaeInventoryService svc;

    /** Override this method to test a different implementation. */
    protected GmaeInventoryService createService() {
        return new FakeInventoryService();
    }

    @BeforeEach
    void setUp() {
        svc = createService();
    }

    // ── add + list ─────────────────────────────────────────────

    @Test
    void addThenList_showsItemWithCorrectQty() {
        svc.addItem(PlayerId.P1, "Potion", "Heals 50 HP", 3);

        List<ItemView> items = svc.listItems(PlayerId.P1);
        assertEquals(1, items.size());
        assertEquals("Potion", items.get(0).name());
        assertEquals(3, items.get(0).quantity());
        assertEquals("Heals 50 HP", items.get(0).description());
    }

    @Test
    void addSameName_stacksQuantity() {
        svc.addItem(PlayerId.P1, "Potion", "Heals 50 HP", 2);
        svc.addItem(PlayerId.P1, "Potion", "Heals 50 HP", 5);

        List<ItemView> items = svc.listItems(PlayerId.P1);
        assertEquals(1, items.size());
        assertEquals(7, items.get(0).quantity());
    }

    @Test
    void addDifferentNames_separateEntries() {
        svc.addItem(PlayerId.P1, "Potion", "", 1);
        svc.addItem(PlayerId.P1, "Sword", "", 1);

        assertEquals(2, svc.listItems(PlayerId.P1).size());
    }

    // ── remove ─────────────────────────────────────────────────

    @Test
    void removePartial_decreasesQuantity() {
        svc.addItem(PlayerId.P1, "Potion", "", 5);

        assertTrue(svc.removeItem(PlayerId.P1, "Potion", 2));

        List<ItemView> items = svc.listItems(PlayerId.P1);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).quantity());
    }

    @Test
    void removeExact_removesItemEntirely() {
        svc.addItem(PlayerId.P1, "Potion", "", 3);

        assertTrue(svc.removeItem(PlayerId.P1, "Potion", 3));
        assertTrue(svc.listItems(PlayerId.P1).isEmpty());
    }

    @Test
    void removeTooMany_returnsFalseAndDoesNotMutate() {
        svc.addItem(PlayerId.P1, "Potion", "", 2);

        assertFalse(svc.removeItem(PlayerId.P1, "Potion", 5));

        List<ItemView> items = svc.listItems(PlayerId.P1);
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).quantity(), "quantity must be unchanged after failed remove");
    }

    @Test
    void removeNonexistent_returnsFalse() {
        assertFalse(svc.removeItem(PlayerId.P1, "Ghost Item", 1));
    }

    // ── player isolation ───────────────────────────────────────

    @Test
    void inventories_areIndependentPerPlayer() {
        svc.addItem(PlayerId.P1, "Sword", "", 1);
        svc.addItem(PlayerId.P2, "Shield", "", 1);

        assertEquals(1, svc.listItems(PlayerId.P1).size());
        assertEquals("Sword", svc.listItems(PlayerId.P1).get(0).name());

        assertEquals(1, svc.listItems(PlayerId.P2).size());
        assertEquals("Shield", svc.listItems(PlayerId.P2).get(0).name());
    }

    @Test
    void removeFromOnePlayer_doesNotAffectOther() {
        svc.addItem(PlayerId.P1, "Potion", "", 3);
        svc.addItem(PlayerId.P2, "Potion", "", 3);

        svc.removeItem(PlayerId.P1, "Potion", 3);

        assertTrue(svc.listItems(PlayerId.P1).isEmpty());
        assertEquals(3, svc.listItems(PlayerId.P2).get(0).quantity());
    }

    // ── empty state ────────────────────────────────────────────

    @Test
    void listItems_emptyByDefault() {
        assertTrue(svc.listItems(PlayerId.P1).isEmpty());
        assertTrue(svc.listItems(PlayerId.P2).isEmpty());
    }
}
