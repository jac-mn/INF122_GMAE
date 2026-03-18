package gmae.adapters;

import gmae.core.services.GmaeRealmService;
import gmae.core.services.RealmContractTest;

/**
 * Adapter contract test skeleton for sub-team #2.
 *
 * <p>Override {@link #createService()} to return your real realm adapter.
 * All invariants from {@link RealmContractTest} are inherited automatically.</p>
 */
class RealmAdapterContractTest extends RealmContractTest {

    @Override
    protected GmaeRealmService createService() {
        // TODO (sub-team #2): replace with your real adapter, e.g.:
        //   return new RealmAdapter(legacyRealmService);
        return super.createService();   // falls back to FakeRealmService for now
    }
}
