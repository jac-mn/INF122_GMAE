package gmae.adapters;

import gmae.core.api.PlayerId;
import gmae.core.model.ItemView;
import gmae.core.services.GmaeInventoryService;
import guildquest.inventory.Inventory;
import guildquest.inventory.Items;

import java.util.*;

/**
 * Adapter that wraps the legacy {@link Inventory} subsystem behind the
 * GMAE {@link GmaeInventoryService} interface.
 *
 * <p><b>Reuse note:</b> The legacy {@code Inventory} and {@code Items}
 * classes in {@code guildquest.inventory} were ported from a previous
 * Python GuildQuest assignment. This adapter bridges the legacy API
 * (single inventory list, no quantity tracking) to the GMAE contract
 * (per-player inventories, quantity stacking, {@link ItemView} DTOs).</p>
 *
 * <h3>Design decisions</h3>
 * <ul>
 *   <li>The legacy model has no concept of quantity — each {@code Items}
 *       instance represents one unit. This adapter tracks quantity by
 *       counting duplicate names and collapses them into a single
 *       {@link ItemView} with the summed quantity.</li>
 *   <li>Per-player separation is handled by maintaining a separate
 *       {@link Inventory} instance for each {@link PlayerId}.</li>
 * </ul>
 */
public class InventoryAdapter implements GmaeInventoryService {

    private final Map<PlayerId, Inventory> inventories = new EnumMap<>(PlayerId.class);

    public InventoryAdapter() {
        inventories.put(PlayerId.P1, new Inventory());
        inventories.put(PlayerId.P2, new Inventory());
    }

    @Override
    public List<ItemView> listItems(PlayerId player) {
        Inventory inv = inventories.get(player);
        // Collapse legacy Items (no qty) into ItemViews with stacked quantity
        Map<String, ItemView> collapsed = new LinkedHashMap<>();

        for (Items legacyItem : inv.getItemsInInventory()) {
            String key = legacyItem.getName().toLowerCase();
            ItemView existing = collapsed.get(key);
            if (existing != null) {
                // Stack: increment quantity
                collapsed.put(key, new ItemView(
                        existing.id(),
                        existing.name(),
                        existing.description(),
                        existing.quantity() + 1
                ));
            } else {
                String id = player + ":" + key;
                collapsed.put(key, new ItemView(
                        id,
                        legacyItem.getName(),
                        legacyItem.getDescription(),
                        1
                ));
            }
        }
        return List.copyOf(collapsed.values());
    }

    @Override
    public void addItem(PlayerId player, String name, String description, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive");

        Inventory inv = inventories.get(player);
        // Legacy model has no quantity — add one Items instance per unit
        for (int i = 0; i < qty; i++) {
            inv.inventoryAdd(new Items(name, "", description));
        }
    }

    @Override
    public boolean removeItem(PlayerId player, String itemName, int qty) {
        if (qty <= 0) return false;

        Inventory inv = inventories.get(player);
        // Count how many of this item exist in legacy inventory
        long count = inv.getItemsInInventory().stream()
                .filter(it -> it.getName().equalsIgnoreCase(itemName))
                .count();

        if (count < qty) return false;

        // Remove qty instances from legacy inventory
        for (int i = 0; i < qty; i++) {
            inv.inventoryRemove(itemName);
        }
        return true;
    }
}
