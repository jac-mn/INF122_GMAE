package gmae.core.model;

import java.util.Objects;

/**
 * Immutable snapshot of a realm — identity and descriptive data only,
 * no spatial or time-rule internals.
 */
public final class RealmView {

    private final String id;
    private final String name;
    private final String description;

    public RealmView(String id, String name, String description) {
        this.id          = Objects.requireNonNull(id);
        this.name        = Objects.requireNonNull(name);
        this.description = description == null ? "" : description;
    }

    public String id()          { return id; }
    public String name()        { return name; }
    public String description() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealmView that)) return false;
        return id.equals(that.id) && name.equals(that.name)
                && description.equals(that.description);
    }

    @Override public int hashCode() { return Objects.hash(id, name, description); }

    @Override
    public String toString() {
        return "RealmView[" + name + "]";
    }
}
