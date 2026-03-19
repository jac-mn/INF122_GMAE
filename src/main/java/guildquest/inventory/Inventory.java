package guildquest.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Legacy inventory model — ported from Python (Assignment X).
 *
 * <p>Original Python class maintained a list of {@link Items} with
 * add, remove (by name), and update operations. This Java port
 * preserves that design exactly.</p>
 */
public class Inventory {

    private final List<Items> itemsInInventory;

    public Inventory() {
        this.itemsInInventory = new ArrayList<>();
    }

    public Inventory(List<Items> itemsInInventory) {
        this.itemsInInventory = new ArrayList<>(itemsInInventory);
    }

    public void inventoryAdd(Items itemToAdd) {
        itemsInInventory.add(itemToAdd);
    }

    public boolean inventoryRemove(String itemName) {
        for (int i = 0; i < itemsInInventory.size(); i++) {
            if (itemsInInventory.get(i).getName().equalsIgnoreCase(itemName)) {
                itemsInInventory.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean inventoryUpdate(String oldName, String newName, String newDescription) {
        for (Items it : itemsInInventory) {
            if (it.getName().equalsIgnoreCase(oldName)) {
                if (newName != null) {
                    it.setName(newName);
                }
                if (newDescription != null) {
                    it.setDescription(newDescription);
                }
                return true;
            }
        }
        return false;
    }

    public List<Items> getItemsInInventory() {
        return Collections.unmodifiableList(itemsInInventory);
    }
}
