package gmae.adapters;

import gmae.core.services.GmaeRealmService;
import gmae.core.services.RealmContractTest;

/**
 * Contract test for the real {@link RealmAdapter}.
 *
 * <p>Inherits every invariant from {@link RealmContractTest}.
 * The adapter manages realm data, player positions, and entity
 * placement behind the GMAE service interface.</p>
 */
class RealmAdapterContractTest extends RealmContractTest {

    @Override
    protected GmaeRealmService createService() {
        return new RealmAdapter();
    }
}
