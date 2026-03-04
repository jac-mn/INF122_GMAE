package gmae.core.engine;

import gmae.core.api.InputEvent;

/**
 * Converts a raw console string into an {@link InputEvent}.
 *
 * <h3>Shortcut keys</h3>
 * Single-character WASD / IJKL inputs are mapped to directional MOVE events
 * so either player can use familiar keys when prompted individually.
 *
 * <h3>Text commands</h3>
 * Multi-word input is split into {@code ACTION PAYLOAD}
 * (e.g. {@code "use potion"} → {@code InputEvent.of("USE", "potion")}).
 * A single word becomes an action with no payload.
 * Empty / blank input becomes {@code PASS}.
 */
public class InputRouter {

    public InputEvent parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return InputEvent.of("PASS");
        }

        String trimmed = raw.trim();

        if (trimmed.length() == 1) {
            InputEvent shortcut = tryMovementShortcut(trimmed.charAt(0));
            if (shortcut != null) return shortcut;
            return InputEvent.of(trimmed.toUpperCase());
        }

        int space = trimmed.indexOf(' ');
        if (space < 0) {
            return InputEvent.of(trimmed.toUpperCase());
        }
        return InputEvent.of(
                trimmed.substring(0, space).toUpperCase(),
                trimmed.substring(space + 1).trim()
        );
    }

    private InputEvent tryMovementShortcut(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'w', 'i' -> InputEvent.of("MOVE", "NORTH");
            case 'a', 'j' -> InputEvent.of("MOVE", "WEST");
            case 's', 'k' -> InputEvent.of("MOVE", "SOUTH");
            case 'd', 'l' -> InputEvent.of("MOVE", "EAST");
            default -> null;
        };
    }
}
