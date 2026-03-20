package gmae.ui.gui;

import gmae.core.api.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Captures keyboard input for two local players using Swing Key Bindings
 * ({@link InputMap}/{@link ActionMap} with {@code WHEN_IN_FOCUSED_WINDOW}).
 *
 * <p>P1 uses WASD, P2 uses arrow keys. Uses a latched+held hybrid model:
 * a key press latches the direction (surviving key release), and holding a key
 * provides continuous movement via the held-direction fallback.</p>
 *
 * <h3>Direction mapping</h3>
 * <pre>
 *   W / Arrow Up    → NORTH (y-1, visually up)
 *   S / Arrow Down  → SOUTH (y+1, visually down)
 *   A / Arrow Left  → WEST  (x-1, visually left)
 *   D / Arrow Right → EAST  (x+1, visually right)
 * </pre>
 */
public class KeyInputHandler {

    // Held state: set on press, cleared on release.
    private volatile String p1Direction;
    private volatile String p2Direction;

    // Latched state: set on fresh press only, cleared when tick consumes it.
    // Survives key release so quick taps are never missed.
    private volatile String p1Latched;
    private volatile String p2Latched;

    private volatile boolean escapePressed;

    public void install(JComponent target) {
        InputMap im  = target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = target.getActionMap();

        // P1: WASD
        bindPress(im, am,   KeyEvent.VK_W, "p1-up",      () -> pressP1("NORTH"));
        bindPress(im, am,   KeyEvent.VK_S, "p1-down",    () -> pressP1("SOUTH"));
        bindPress(im, am,   KeyEvent.VK_A, "p1-left",    () -> pressP1("WEST"));
        bindPress(im, am,   KeyEvent.VK_D, "p1-right",   () -> pressP1("EAST"));
        bindRelease(im, am, KeyEvent.VK_W, "p1-up-r",    () -> releaseP1("NORTH"));
        bindRelease(im, am, KeyEvent.VK_S, "p1-down-r",  () -> releaseP1("SOUTH"));
        bindRelease(im, am, KeyEvent.VK_A, "p1-left-r",  () -> releaseP1("WEST"));
        bindRelease(im, am, KeyEvent.VK_D, "p1-right-r", () -> releaseP1("EAST"));

        // P2: Arrow keys
        bindPress(im, am,   KeyEvent.VK_UP,    "p2-up",      () -> pressP2("NORTH"));
        bindPress(im, am,   KeyEvent.VK_DOWN,  "p2-down",    () -> pressP2("SOUTH"));
        bindPress(im, am,   KeyEvent.VK_LEFT,  "p2-left",    () -> pressP2("WEST"));
        bindPress(im, am,   KeyEvent.VK_RIGHT, "p2-right",   () -> pressP2("EAST"));
        bindRelease(im, am, KeyEvent.VK_UP,    "p2-up-r",    () -> releaseP2("NORTH"));
        bindRelease(im, am, KeyEvent.VK_DOWN,  "p2-down-r",  () -> releaseP2("SOUTH"));
        bindRelease(im, am, KeyEvent.VK_LEFT,  "p2-left-r",  () -> releaseP2("WEST"));
        bindRelease(im, am, KeyEvent.VK_RIGHT, "p2-right-r", () -> releaseP2("EAST"));

        // ESC
        bindPress(im, am, KeyEvent.VK_ESCAPE, "esc", () -> escapePressed = true);
    }

    /**
     * Returns a MOVE event for P1, or null if idle.
     * Prefers the latched value (captures quick taps), falls back to held
     * direction (provides continuous movement while key is held).
     */
    public InputEvent pollP1() {
        String dir = p1Latched;
        p1Latched = null;
        if (dir == null) dir = p1Direction;
        return dir != null ? InputEvent.of("MOVE", dir) : null;
    }

    /**
     * Returns a MOVE event for P2, or null if idle.
     * Same latch-then-held logic as P1.
     */
    public InputEvent pollP2() {
        String dir = p2Latched;
        p2Latched = null;
        if (dir == null) dir = p2Direction;
        return dir != null ? InputEvent.of("MOVE", dir) : null;
    }

    public boolean isEscapePressed() { return escapePressed; }
    public void resetEscape()        { escapePressed = false; }

    /** Clears all held/latched directions and escape state. Call on game restart. */
    public void clearAll() {
        p1Direction = null;
        p2Direction = null;
        p1Latched   = null;
        p2Latched   = null;
        escapePressed = false;
    }

    // ── Press / Release internals ───────────────────────────────────

    private void pressP1(String dir) {
        if (p1Direction == null) p1Latched = dir;
        p1Direction = dir;
    }

    private void pressP2(String dir) {
        if (p2Direction == null) p2Latched = dir;
        p2Direction = dir;
    }

    private void releaseP1(String dir) {
        if (dir.equals(p1Direction)) p1Direction = null;
    }

    private void releaseP2(String dir) {
        if (dir.equals(p2Direction)) p2Direction = null;
    }

    private static void bindPress(InputMap im, ActionMap am,
                                  int keyCode, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0, false), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private static void bindRelease(InputMap im, ActionMap am,
                                    int keyCode, String name, Runnable action) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0, true), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }
}
