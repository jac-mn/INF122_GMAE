package gmae.ui.gui;

import gmae.adapters.InventoryAdapter;
import gmae.adapters.RealmAdapter;
import gmae.core.engine.AdventureManager;
import gmae.core.engine.AdventureRegistry;
import gmae.core.engine.AdventureRegistry.EntryInfo;
import gmae.core.services.ServiceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.util.List;
import java.util.Set;

/**
 * Entry point for the GUI mode.
 *
 * <p>Prompts for player names, lets the user choose an adventure, then
 * launches the appropriate GUI (real-time Relic Hunt or turn-based Caravan
 * Trade Run). The two paths are completely independent — each has its own
 * panel, session, and input model.</p>
 */
public class GuiLauncher {

    private static final Set<String> GUI_CAPABLE =
            Set.of("relic-hunt", "caravan-trade-run");

    private final AdventureRegistry registry;

    public GuiLauncher(AdventureRegistry registry) {
        this.registry = registry;
    }

    /** Launches the GUI on the EDT. */
    public void launch() {
        SwingUtilities.invokeLater(this::buildAndShow);
    }

    private void buildAndShow() {
        String p1Name = promptName("Player 1 name:", "Player 1");
        if (p1Name == null) return;
        String p2Name = promptName("Player 2 name:", "Player 2");
        if (p2Name == null) return;

        List<EntryInfo> entries = registry.listAdventures().stream()
                .filter(e -> GUI_CAPABLE.contains(e.id()))
                .toList();
        if (entries.isEmpty()) return;

        String[] titles = entries.stream()
                .map(EntryInfo::title)
                .toArray(String[]::new);

        Object chosen = JOptionPane.showInputDialog(null,
                "Choose an adventure:",
                "GMAE \u2014 Adventure Select",
                JOptionPane.QUESTION_MESSAGE,
                null, titles, titles[0]);
        if (chosen == null) return;

        String chosenId = entries.stream()
                .filter(e -> e.title().equals(chosen.toString()))
                .findFirst()
                .orElseThrow()
                .id();

        ServiceBundle services = ServiceBundle.builder()
                .realmService(new RealmAdapter())
                .inventoryService(new InventoryAdapter())
                .build();
        AdventureManager manager = new AdventureManager(registry, services);

        switch (chosenId) {
            case "relic-hunt"        -> launchRelicHunt(manager, p1Name, p2Name);
            case "caravan-trade-run" -> launchCaravan(manager, p1Name, p2Name);
        }
    }

    // ── Relic Hunt (real-time, unchanged) ────────────────────────────

    private void launchRelicHunt(AdventureManager manager,
                                 String p1Name, String p2Name) {
        JFrame frame = new JFrame("GMAE \u2014 Relic Hunt");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel panel = new GamePanel(p1Name, p2Name);
        frame.add(panel);

        KeyInputHandler keyHandler = new KeyInputHandler();
        keyHandler.install(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new GuiSession(manager, keyHandler, panel, frame,
                       p1Name, p2Name, "relic-hunt").start();
    }

    // ── Caravan Trade Run (turn-based) ──────────────────────────────

    private void launchCaravan(AdventureManager manager,
                               String p1Name, String p2Name) {
        JFrame frame = new JFrame("GMAE \u2014 Caravan Trade Run");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        CaravanPanel panel = new CaravanPanel(p1Name, p2Name);
        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new CaravanSession(manager, panel, frame,
                           p1Name, p2Name, "caravan-trade-run").start();
    }

    // ── Shared helpers ──────────────────────────────────────────────

    private static String promptName(String message, String defaultName) {
        String input = JOptionPane.showInputDialog(null, message, defaultName);
        if (input == null) return null;
        input = input.trim();
        return input.isEmpty() ? defaultName : input;
    }
}
