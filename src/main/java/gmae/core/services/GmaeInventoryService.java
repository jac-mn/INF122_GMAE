package gmae.core.services;

import gmae.core.api.PlayerId;
import gmae.core.model.ItemView;

import java.util.List;

/**
 * Provides per-player inventory operations to adventures.
 *
 * <p>Adapters (sub-team #2) will implement this by wrapping the legacy
 * {@code guildquest} inventory subsystem. Adventures see only
 * {@link ItemView} DTOs and {@link PlayerId} — never legacy types.</p>
 */
public interface GmaeInventoryService {

    /**
     * Lists all items in the given player's inventory.
     *
     * @param player which player's inventory to query
     * @return unmodifiable list of item snapshots (empty if none)
     */
    List<ItemView> listItems(PlayerId player);

    /**
     * Adds (or stacks) an item into the player's inventory.
     *
     * @param player      target player
     * @param name        item name
     * @param description item description
     * @param qty         quantity to add (must be positive)
     */
    void addItem(PlayerId player, String name, String description, int qty);

    /**
     * Removes a quantity of the named item from the player's inventory.
     *
     * @param player       target player
     * @param itemName     name of the item to remove
     * @param qty          quantity to remove
     * @return {@code true} if the removal succeeded, {@code false} if the
     *         item was not found or insufficient quantity
     */
    boolean removeItem(PlayerId player, String itemName, int qty);
}
