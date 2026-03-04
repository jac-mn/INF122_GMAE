package gmae.core.services.fakes;

import gmae.core.api.PlayerId;
import gmae.core.model.ItemView;
import gmae.core.services.GmaeInventoryService;

import java.util.*;

/**
 * Deterministic in-memory implementation of {@link GmaeInventoryService}.
 *
 * <p>Maintains separate inventories for P1 and P2. Items with the same
 * name (case-insensitive) are stacked by quantity. Item ids are
 * deterministic: {@code "P1:potion"}, {@code "P2:sword"}, etc.</p>
 */
public class FakeInventoryService implements GmaeInventoryService {

    private final Map<PlayerId, List<MutableItem>> inventories = new EnumMap<>(PlayerId.class);

    public FakeInventoryService() {
        inventories.put(PlayerId.P1, new ArrayList<>());
        inventories.put(PlayerId.P2, new ArrayList<>());
    }

    @Override
    public List<ItemView> listItems(PlayerId player) {
        return inventories.get(player).stream()
                .map(mi -> new ItemView(mi.id, mi.name, mi.description, mi.qty))
                .toList();
    }

    @Override
    public void addItem(PlayerId player, String name, String description, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive");

        List<MutableItem> inv = inventories.get(player);
        for (MutableItem mi : inv) {
            if (mi.name.equalsIgnoreCase(name)) {
                mi.qty += qty;
                return;
            }
        }
        inv.add(new MutableItem(player + ":" + name.toLowerCase(), name, description, qty));
    }

    @Override
    public boolean removeItem(PlayerId player, String itemName, int qty) {
        if (qty <= 0) return false;

        List<MutableItem> inv = inventories.get(player);
        for (Iterator<MutableItem> it = inv.iterator(); it.hasNext(); ) {
            MutableItem mi = it.next();
            if (mi.name.equalsIgnoreCase(itemName)) {
                if (mi.qty < qty) return false;
                mi.qty -= qty;
                if (mi.qty == 0) it.remove();
                return true;
            }
        }
        return false;
    }

    private static class MutableItem {
        final String id;
        final String name;
        final String description;
        int qty;

        MutableItem(String id, String name, String description, int qty) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.qty = qty;
        }
    }
}
