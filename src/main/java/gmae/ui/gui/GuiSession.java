package gmae.ui.gui;

import gmae.core.api.InputEvent;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.engine.AdventureManager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Drives the real-time game loop for GUI mode.
 *
 * <p>A {@link javax.swing.Timer} fires every {@value #TICK_MS}ms on the EDT.
 * Each tick polls keyboard input for both players simultaneously. The adventure
 * only advances when at least one player has input, preventing idle "pass"
 * messages from flooding the display.</p>
 *
 * <p>There is no time limit — the game continues until the adventure reports
 * completion (a player collects enough relics to win).</p>
 */
public class GuiSession {

    private static final int TICK_MS = 150;

    private final AdventureManager manager;
    private final KeyInputHandler  keyHandler;
    private final GamePanel        gamePanel;
    private final JFrame           frame;
    private final String           p1Name;
    private final String           p2Name;
    private final String           adventureId;

    private Timer gameTimer;
    private int   elapsedMs;
    private AdventureState lastState;

    public GuiSession(AdventureManager manager, KeyInputHandler keyHandler,
                      GamePanel gamePanel, JFrame frame,
                      String p1Name, String p2Name, String adventureId) {
        this.manager     = manager;
        this.keyHandler  = keyHandler;
        this.gamePanel   = gamePanel;
        this.frame       = frame;
        this.p1Name      = p1Name;
        this.p2Name      = p2Name;
        this.adventureId = adventureId;
    }

    /** Initialises the adventure and starts the tick timer. */
    public void start() {
        elapsedMs = 0;
        lastState = manager.start(adventureId);
        gamePanel.updateState(lastState, elapsedMs);
        gamePanel.repaint();

        gameTimer = new Timer(TICK_MS, e -> tick());
        gameTimer.start();
    }

    // ── Tick loop ───────────────────────────────────────────────────

    private void tick() {
        if (keyHandler.isEscapePressed()) {
            gameTimer.stop();
            keyHandler.resetEscape();
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Quit the game?", "GMAE",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                frame.dispose();
            } else {
                gameTimer.start();
            }
            return;
        }

        elapsedMs += TICK_MS;

        InputEvent p1 = keyHandler.pollP1();
        InputEvent p2 = keyHandler.pollP2();

        if (p1 != null) manager.submitInput(PlayerId.P1, p1);
        if (p2 != null) manager.submitInput(PlayerId.P2, p2);

        if (p1 != null || p2 != null) {
            lastState = manager.step();
            gamePanel.updateState(lastState, elapsedMs);

            if (lastState.complete()) {
                gameTimer.stop();
                gamePanel.paintImmediately(
                        0, 0, gamePanel.getWidth(), gamePanel.getHeight());
                showResult(lastState);
                return;
            }
        }

        gamePanel.repaint();
    }

    // ── End-game handling ───────────────────────────────────────────

    private void showResult(AdventureState state) {
        String msg = switch (state.outcome()) {
            case P1_WINS  -> p1Name + " wins!";
            case P2_WINS  -> p2Name + " wins!";
            case DRAW     -> "It's a draw!";
            case COOP_WIN -> "Both players win!";
            case LOSS     -> "Both players lose!";
            default       -> state.outcome().toString();
        };
        offerReplay(msg);
    }

    private void offerReplay(String resultMessage) {
        int choice = JOptionPane.showOptionDialog(frame,
                resultMessage + "\n\nPlay again?",
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
        elapsedMs = 0;
        gamePanel.resetRelics();
        keyHandler.clearAll();
        lastState = manager.restart();
        gamePanel.updateState(lastState, elapsedMs);
        gamePanel.repaint();
        gameTimer.start();
    }
}
