package gmae.adapters;

import gmae.core.services.GmaeQuestEventService;
import gmae.core.services.QuestEventContractTest;

/**
 * Contract test for the quest-event adapter layer.
 *
 * <p>Inherits every invariant from {@link QuestEventContractTest}.
 * Currently validated against the fake implementation; swap in a real
 * adapter when the legacy quest-event subsystem is integrated.</p>
 */
class QuestEventAdapterContractTest extends QuestEventContractTest {

    @Override
    protected GmaeQuestEventService createService() {
        return super.createService();
    }
}
