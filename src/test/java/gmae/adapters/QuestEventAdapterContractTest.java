package gmae.adapters;

import gmae.core.services.GmaeQuestEventService;
import gmae.core.services.QuestEventContractTest;

/**
 * Adapter contract test skeleton for sub-team #2.
 *
 * <p>Override {@link #createService()} to return your real quest-event
 * adapter. All invariants from {@link QuestEventContractTest} are inherited
 * automatically.</p>
 */
class QuestEventAdapterContractTest extends QuestEventContractTest {

    @Override
    protected GmaeQuestEventService createService() {
        // TODO (sub-team #2): replace with your real adapter, e.g.:
        //   return new QuestEventAdapter(legacyCampaignService, legacyStore);
        return super.createService();   // falls back to FakeQuestEventService for now
    }
}
