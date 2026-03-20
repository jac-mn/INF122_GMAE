package gmae.ui.gui;

import gmae.core.api.InputEvent;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.engine.AdventureManager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Turn-based controller for the Caravan Trade Run GUI.
 *
 * <p>Implements a strict <b>P1_INPUT → P2_INPUT → RESOLVE</b> cycle.
 * Each player's command is parsed from text and buffered via
 * {@link AdventureManager#submitInput}. After both players have acted,
 * {@link AdventureManager#step()} advances the adventure and the panel
 * is refreshed from the resulting {@link AdventureState}.</p>
 */
public class CaravanSession {

    private final AdventureManager manager;
    private final CaravanPanel     panel;
    private final JFrame           frame;
    private final String           p1Name;
    private final String           p2Name;
    private final String           adventureId;

    private boolean isP1Turn;

    public CaravanSession(AdventureManager manager, CaravanPanel panel,
                          JFrame frame, String p1Name, String p2Name,
                          String adventureId) {
        this.manager     = manager;
        this.panel       = panel;
        this.frame       = frame;
        this.p1Name      = p1Name;
        this.p2Name      = p2Name;
        this.adventureId = adventureId;
    }

    /** Starts the adventure and wires the command listener. */
    public void start() {
        isP1Turn = true;
        AdventureState initial = manager.start(adventureId);
        panel.updateState(initial);
        panel.setPhase(true, p1Name);
        panel.setCommandListener(this::onCommand);
    }

    // ── Command handling ────────────────────────────────────────────

    private void onCommand(String raw) {
        InputEvent event = parseCommand(raw);

        if (isP1Turn) {
            manager.submitInput(PlayerId.P1, event);
            isP1Turn = false;
            panel.setPhase(false, p2Name);
        } else {
            manager.submitInput(PlayerId.P2, event);

            AdventureState state = manager.step();
            panel.updateState(state);

            if (state.complete()) {
                showResult(state);
            } else {
                isP1Turn = true;
                panel.setPhase(true, p1Name);
            }
        }
    }

    /**
     * Splits raw text into an {@link InputEvent}.
     * First token becomes the action, the rest is the payload.
     * Blank input maps to PASS.
     */
    private static InputEvent parseCommand(String raw) {
        if (raw == null) raw = "";
        raw = raw.trim();
        if (raw.isEmpty()) return InputEvent.of("PASS");

        int space = raw.indexOf(' ');
        if (space < 0) return InputEvent.of(raw.toUpperCase());

        String action  = raw.substring(0, space).toUpperCase();
        String payload = raw.substring(space + 1).trim();
        return payload.isEmpty()
                ? InputEvent.of(action)
                : InputEvent.of(action, payload);
    }

    // ── End-game ────────────────────────────────────────────────────

    private void showResult(AdventureState state) {
        String msg = switch (state.outcome()) {
            case P1_WINS  -> p1Name + " wins!";
            case P2_WINS  -> p2Name + " wins!";
            case DRAW     -> "It's a draw!";
            case COOP_WIN -> "Both players win!";
            case LOSS     -> "Both players lose!";
            default       -> state.outcome().toString();
        };

        int choice = JOptionPane.showOptionDialog(frame,
                msg + "\n\nPlay again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Play Again", "Quit"},
                "Play Again");

        if (choice == 0) {
            restart();
        } else {
            frame.dispose();
        }
    }

    private void restart() {
        isP1Turn = true;
        AdventureState state = manager.restart();
        panel.updateState(state);
        panel.setPhase(true, p1Name);
    }
}
