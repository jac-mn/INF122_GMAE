package gmae.adapters;

import gmae.core.services.GmaeInventoryService;
import gmae.core.services.InventoryContractTest;

/**
 * Adapter contract test skeleton for sub-team #2.
 *
 * <p><b>How to use:</b></p>
 * <ol>
 *   <li>Implement your real adapter (e.g. {@code InventoryAdapter implements
 *       GmaeInventoryService}) in {@code src/main/java/gmae/adapters/}.</li>
 *   <li>Override {@link #createService()} below to return your adapter.</li>
 *   <li>Run this test — it inherits every invariant from
 *       {@link InventoryContractTest}.</li>
 * </ol>
 *
 * <p>Once your adapter passes all inherited tests, you can be confident it
 * satisfies the same contract as the reference fake.</p>
 */
class InventoryAdapterContractTest extends InventoryContractTest {

    @Override
    protected GmaeInventoryService createService() {
        // TODO (sub-team #2): replace with your real adapter, e.g.:
        //   return new InventoryAdapter(legacyStore, playerMapping);
        return super.createService();   // falls back to FakeInventoryService for now
    }
}
