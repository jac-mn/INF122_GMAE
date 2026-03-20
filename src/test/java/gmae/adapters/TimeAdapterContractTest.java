package gmae.adapters;

import gmae.core.services.GmaeTimeService;
import gmae.core.services.TimeContractTest;

/**
 * Contract test for the time adapter layer.
 *
 * <p>Inherits every invariant from {@link TimeContractTest}.
 * Currently validated against the fake implementation; swap in a real
 * adapter when the legacy time subsystem is integrated.</p>
 */
class TimeAdapterContractTest extends TimeContractTest {

    @Override
    protected GmaeTimeService createService() {
        return super.createService();
    }
}
