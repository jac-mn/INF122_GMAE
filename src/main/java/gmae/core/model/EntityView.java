package gmae.core.model;

import java.util.Objects;

/**
 * Immutable snapshot of an entity in the realm — identity, location, and metadata.
 *
 * <p>Represents NPCs, objects, or other spatial entities that adventures need to
 * see. Contains no legacy types — only primitives and engine DTOs.</p>
 */
public final class EntityView {

    private final String id;
    private final String name;
    private final String type;          // e.g., "npc", "treasure", "obstacle"
    private final Coord position;
    private final String realm;

    /**
     * Creates an entity view.
     *
     * @param id       stable unique identifier for this entity
     * @param name     display name
     * @param type     entity classification
     * @param position coordinate in realm space
     * @param realm    name of the realm this entity inhabits
     */
    public EntityView(String id, String name, String type, Coord position, String realm) {
        this.id       = Objects.requireNonNull(id);
        this.name     = Objects.requireNonNull(name);
        this.type     = Objects.requireNonNull(type);
        this.position = Objects.requireNonNull(position);
        this.realm    = realm == null ? "" : realm;
    }

    public String id()       { return id; }
    public String name()     { return name; }
    public String type()     { return type; }
    public Coord position()  { return position; }
    public String realm()    { return realm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityView that)) return false;
        return id.equals(that.id) && name.equals(that.name)
                && type.equals(that.type) && position.equals(that.position)
                && realm.equals(that.realm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, position, realm);
    }

    @Override
    public String toString() {
        return "EntityView[" + name + " (" + type + ") @ " + position + "]";
    }
}
