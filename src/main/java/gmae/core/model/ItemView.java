package gmae.core.model;

import java.util.Objects;

/**
 * Immutable snapshot of a single inventory item.
 *
 * <p>Contains no legacy types — only primitives and Strings.</p>
 */
public final class ItemView {

    private final String id;
    private final String name;
    private final String description;
    private final int    quantity;

    public ItemView(String id, String name, String description, int quantity) {
        this.id          = Objects.requireNonNull(id);
        this.name        = Objects.requireNonNull(name);
        this.description = description == null ? "" : description;
        this.quantity    = quantity;
    }

    public String id()          { return id; }
    public String name()        { return name; }
    public String description() { return description; }
    public int    quantity()    { return quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemView that)) return false;
        return quantity == that.quantity
                && id.equals(that.id)
                && name.equals(that.name)
                && description.equals(that.description);
    }

    @Override public int hashCode() { return Objects.hash(id, name, description, quantity); }

    @Override
    public String toString() {
        return "ItemView[" + name + " x" + quantity + "]";
    }
}
