package gmae.core.services;

import gmae.core.model.QuestEventView;

import java.util.List;

/**
 * Provides quest-event queries and mutations to adventures.
 *
 * <p>Adapters (sub-team #2) will implement this by wrapping the legacy
 * {@code guildquest} campaign/event subsystem. Adventures see only
 * {@link QuestEventView} DTOs — never legacy types.</p>
 *
 * <p><em>Optional service</em> — not every engine configuration will
 * provide this. Adventures should check for its presence via
 * {@link ServiceBundle#questEventService()}.</p>
 */
public interface GmaeQuestEventService {

    /** Lists all quest events visible to the current session. */
    List<QuestEventView> listEvents();

    /**
     * Creates a new quest event.
     *
     * @param event the event data (the {@code id} field is ignored; a new
     *              id is generated server-side)
     * @return the generated event id
     */
    String addEvent(QuestEventView event);

    /**
     * Removes a quest event by id.
     *
     * @param eventId the event to remove
     * @return {@code true} if the event existed and was removed
     */
    boolean removeEvent(String eventId);
}
