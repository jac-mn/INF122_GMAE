package gmae.core.services;

import gmae.core.api.PlayerId;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.RealmView;

import java.util.List;
import java.util.Optional;

/**
 * Provides realm/map queries and player movement operations to adventures.
 *
 * <p>Adapters implement this by wrapping the legacy {@code guildquest}
 * realm/spatial subsystem. Adventures see only engine DTOs
 * ({@link RealmView}, {@link Coord}, {@link EntityView}) — never legacy types.</p>
 */
public interface GmaeRealmService {

    /**
     * Lists all known realms.
     *
     * @return unmodifiable list of realm snapshots
     */
    List<RealmView> listRealms();

    /**
     * Retrieves the current position of a player in their realm.
     *
     * @param player which player
     * @return the player's current coordinate, or empty if not yet placed
     */
    Optional<Coord> playerPosition(PlayerId player);

    /**
     * Places a player at a given coordinate in a named realm.
     *
     * <p>If the player is already placed, this moves them to the new position.</p>
     *
     * @param player    target player
     * @param realm     name of the destination realm
     * @param position  coordinate in that realm
     */
    void placePlayer(PlayerId player, String realm, Coord position);

    /**
     * Moves a player by a relative offset (delta).
     *
     * <p>Returns false if the move would take the player out of bounds or
     * hit an obstacle.</p>
     *
     * @param player target player
     * @param dx     change in x
     * @param dy     change in y
     * @return true if the move succeeded, false if blocked or player not placed
     */
    boolean movePlayer(PlayerId player, int dx, int dy);

    /**
     * Lists all entities visible in a given realm.
     *
     * @param realm name of the realm
     * @return unmodifiable list of entity snapshots in that realm
     */
    List<EntityView> listEntitiesInRealm(String realm);

    /**
     * Adds an entity to a realm at a given position.
     *
     * @param entity snapshot of the entity to place
     * @return the entity's id (may be auto-generated if the input id is empty)
     */
    String addEntity(EntityView entity);

    /**
     * Removes an entity by id from all realms.
     *
     * @param entityId  id of the entity to remove
     * @return true if the entity was found and removed
     */
    boolean removeEntity(String entityId);
}
