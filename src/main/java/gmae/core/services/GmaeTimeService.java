package gmae.core.services;

import gmae.core.model.TimeView;

import java.util.Optional;

/**
 * Provides world-time queries and time advancement to adventures.
 *
 * <p>Adapters implement this by wrapping the legacy {@code guildquest}
 * time subsystem. Adventures see only {@link TimeView} DTOs — never
 * legacy types.</p>
 */
public interface GmaeTimeService {

    /** Returns the current world time as a snapshot. */
    TimeView nowWorld();

    /**
     * Returns the current time converted to the named realm's local time.
     *
     * @param realmName display name of the realm
     * @return the time view including a local-time string, or empty if
     *         the realm is not found
     */
    Optional<TimeView> toLocal(String realmName);

    /**
     * Advances the world clock by the given number of minutes.
     *
     * @param minutes positive number of minutes to advance
     */
    void advanceMinutes(int minutes);
}
