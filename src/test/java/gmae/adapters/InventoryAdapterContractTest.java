package gmae.adapters;

import gmae.core.services.GmaeInventoryService;
import gmae.core.services.InventoryContractTest;

/**
 * Contract test for the real {@link InventoryAdapter}.
 *
 * <p>Inherits every invariant from {@link InventoryContractTest}.
 * The adapter wraps the legacy {@code guildquest.inventory} subsystem
 * (ported from a prior Python assignment) behind the GMAE service
 * interface.</p>
 */
class InventoryAdapterContractTest extends InventoryContractTest {

    @Override
    protected GmaeInventoryService createService() {
        return new InventoryAdapter();
    }
}
