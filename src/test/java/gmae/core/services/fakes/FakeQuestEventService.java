package gmae.core.services.fakes;

import gmae.core.model.QuestEventView;
import gmae.core.services.GmaeQuestEventService;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic in-memory implementation of {@link GmaeQuestEventService}.
 *
 * <p>Event ids are sequential: {@code "evt-1"}, {@code "evt-2"}, etc.</p>
 */
public class FakeQuestEventService implements GmaeQuestEventService {

    private final List<QuestEventView> events = new ArrayList<>();
    private int nextId = 1;

    @Override
    public List<QuestEventView> listEvents() {
        return List.copyOf(events);
    }

    @Override
    public String addEvent(QuestEventView event) {
        String id = "evt-" + nextId++;
        events.add(new QuestEventView(
                id, event.title(), event.startMinutes(), event.endMinutes(), event.realmName()));
        return id;
    }

    @Override
    public boolean removeEvent(String eventId) {
        return events.removeIf(e -> e.id().equals(eventId));
    }
}
