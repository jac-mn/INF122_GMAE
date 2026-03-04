package gmae.adapters;

import gmae.core.services.GmaeTimeService;
import gmae.core.services.TimeContractTest;

/**
 * Adapter contract test skeleton for sub-team #2.
 *
 * <p>Override {@link #createService()} to return your real time adapter.
 * All invariants from {@link TimeContractTest} are inherited automatically.</p>
 */
class TimeAdapterContractTest extends TimeContractTest {

    @Override
    protected GmaeTimeService createService() {
        // TODO (sub-team #2): replace with your real adapter, e.g.:
        //   return new TimeAdapter(legacyStore);
        return super.createService();   // falls back to FakeTimeService for now
    }
}
