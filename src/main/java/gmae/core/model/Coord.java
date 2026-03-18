package gmae.core.model;

import java.util.Objects;

/**
 * Immutable coordinate DTO representing a position in 2D space.
 *
 * <p>Used by realm/map adventures to track player and entity positions.
 * No spatial calculations are performed here — only storage and equality.</p>
 */
public final class Coord {

    private final int x;
    private final int y;

    /**
     * Creates a coordinate.
     *
     * @param x horizontal position
     * @param y vertical position
     */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coord that)) return false;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Coord[" + x + ", " + y + "]";
    }
}
