package gmae.ui.gui;

import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.PlayerView;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Turn-based GUI panel for Caravan Trade Run.
 *
 * <p>Layout: header (NORTH), trade-map (WEST), message log (CENTER),
 * player-info panels (EAST), and a command bar (SOUTH). All game state
 * is derived from {@link AdventureState} and {@link PlayerView#attributes()}.</p>
 */
public class CaravanPanel extends JPanel {

    static final int PANEL_W = 950;
    static final int PANEL_H = 700;

    // ── Palette ─────────────────────────────────────────────────────
    private static final Color BG        = new Color(42, 42, 42);
    private static final Color HEADER_BG = new Color(32, 32, 32);
    private static final Color MAP_BG    = new Color(38, 50, 38);
    private static final Color LOG_BG    = new Color(30, 30, 30);
    private static final Color INFO_BG   = new Color(38, 38, 42);
    private static final Color CMD_BG    = new Color(35, 35, 35);
    private static final Color P1_COLOR  = new Color(50, 130, 240);
    private static final Color P2_COLOR  = new Color(230, 60, 60);
    private static final Color TEXT      = new Color(220, 220, 220);
    private static final Color DIM_TEXT  = new Color(150, 150, 150);
    private static final Color NODE_BG   = new Color(55, 75, 55);
    private static final Color NODE_EDGE = new Color(90, 130, 90);
    private static final Color ROUTE_CLR = new Color(70, 95, 70);
    private static final Color GOLD      = new Color(255, 215, 0);

    // ── Trade-map data (mirrors CaravanTradeRunAdventure constants) ──
    private static final Map<String, int[]> NODE_POS = Map.of(
            "Marketplace",   new int[]{75,  70},
            "Docks",         new int[]{195, 70},
            "Crossroads",    new int[]{135, 190},
            "Mountain Pass", new int[]{75,  320},
            "Oasis",         new int[]{195, 320}
    );
    private static final Map<String, String> GOODS = Map.of(
            "Marketplace", "Silk",  "Docks", "Fish",
            "Crossroads", "Gems",   "Mountain Pass", "Iron",
            "Oasis", "Spice"
    );
    private static final String[][] EDGES = {
            {"Marketplace", "Docks"},
            {"Marketplace", "Crossroads"},
            {"Docks", "Crossroads"},
            {"Crossroads", "Mountain Pass"},
            {"Crossroads", "Oasis"},
            {"Mountain Pass", "Oasis"}
    };

    // ── UI components ───────────────────────────────────────────────
    private final JLabel     titleLabel;
    private final JLabel     turnLabel;
    private final JLabel     phaseLabel;
    private final MapCanvas  mapCanvas;
    private final JTextArea  messageLog;
    private final JTextArea  p1Info;
    private final JTextArea  p2Info;
    private final JPanel     p1Panel;
    private final JPanel     p2Panel;
    private final JLabel     commandPrompt;
    private final JTextField commandField;

    // ── State ───────────────────────────────────────────────────────
    private final String p1Name;
    private final String p2Name;
    private String p1Location = "";
    private String p2Location = "";
    private int    currentTurn;

    private Consumer<String> onCommand;

    // ── Constructor ─────────────────────────────────────────────────

    public CaravanPanel(String p1Name, String p2Name) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(BG);
        setLayout(new BorderLayout(2, 2));

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(PANEL_W, 42));
        header.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        titleLabel = label("Caravan Trade Run", GOLD, 16, Font.BOLD);
        turnLabel  = label("Turn 0", TEXT, 14, Font.BOLD);
        phaseLabel = label(p1Name + "'s turn  ", P1_COLOR, 14, Font.BOLD);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(turnLabel,  BorderLayout.CENTER);
        header.add(phaseLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Content area ────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout(2, 0));
        content.setBackground(BG);

        // Trade map (left)
        mapCanvas = new MapCanvas();
        mapCanvas.setPreferredSize(new Dimension(270, 0));
        content.add(mapCanvas, BorderLayout.WEST);

        // Message log (center)
        messageLog = new JTextArea();
        messageLog.setEditable(false);
        messageLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        messageLog.setBackground(LOG_BG);
        messageLog.setForeground(TEXT);
        messageLog.setCaretColor(LOG_BG);
        messageLog.setLineWrap(true);
        messageLog.setWrapStyleWord(true);
        messageLog.setMargin(new Insets(8, 10, 8, 10));
        JScrollPane scroll = new JScrollPane(messageLog);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        // Player info (right)
        JPanel infoSide = new JPanel();
        infoSide.setLayout(new BoxLayout(infoSide, BoxLayout.Y_AXIS));
        infoSide.setBackground(BG);
        infoSide.setPreferredSize(new Dimension(230, 0));

        p1Info  = infoArea();
        p1Panel = infoPanel(p1Info, p1Name, P1_COLOR, false);
        infoSide.add(p1Panel);
        infoSide.add(Box.createVerticalStrut(4));

        p2Info  = infoArea();
        p2Panel = infoPanel(p2Info, p2Name, P2_COLOR, false);
        infoSide.add(p2Panel);
        infoSide.add(Box.createVerticalGlue());

        content.add(infoSide, BorderLayout.EAST);
        add(content, BorderLayout.CENTER);

        // ── Command bar ─────────────────────────────────────────────
        JPanel cmdBar = new JPanel(new BorderLayout(4, 4));
        cmdBar.setBackground(CMD_BG);
        cmdBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setBackground(CMD_BG);

        commandPrompt = new JLabel(p1Name + "'s command:");
        commandPrompt.setFont(new Font("SansSerif", Font.BOLD, 13));
        commandPrompt.setForeground(P1_COLOR);
        commandPrompt.setPreferredSize(new Dimension(200, 28));
        inputRow.add(commandPrompt, BorderLayout.WEST);

        commandField = new JTextField();
        commandField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        commandField.addActionListener(e -> submitCommand());
        inputRow.add(commandField, BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> submitCommand());
        inputRow.add(submitBtn, BorderLayout.EAST);
        cmdBar.add(inputRow, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        btnRow.setBackground(CMD_BG);
        for (String cmd : new String[]{"Status", "Look", "Map", "Help", "Deliver", "Pass"}) {
            JButton b = new JButton(cmd);
            b.setFont(new Font("SansSerif", Font.PLAIN, 11));
            b.setMargin(new Insets(2, 8, 2, 8));
            b.addActionListener(e -> fireCommand(cmd.toLowerCase()));
            btnRow.add(b);
        }
        cmdBar.add(btnRow, BorderLayout.SOUTH);
        add(cmdBar, BorderLayout.SOUTH);
    }

    // ── Public API ──────────────────────────────────────────────────

    public void setCommandListener(Consumer<String> listener) {
        this.onCommand = listener;
    }

    /** Refreshes every visual element from the latest adventure state. */
    public void updateState(AdventureState state) {
        currentTurn = state.tickOrTurn();
        turnLabel.setText("Turn " + currentTurn);

        PlayerView p1 = state.players().get(PlayerId.P1);
        PlayerView p2 = state.players().get(PlayerId.P2);
        if (p1 != null) {
            p1Location = p1.attributes().getOrDefault("location", "?");
            p1Info.setText(formatInfo(p1));
        }
        if (p2 != null) {
            p2Location = p2.attributes().getOrDefault("location", "?");
            p2Info.setText(formatInfo(p2));
        }

        List<String> msgs = state.messages().stream()
                .filter(m -> !m.contains("passes"))
                .toList();
        StringBuilder sb = new StringBuilder();
        for (String m : msgs) sb.append(m).append('\n');
        messageLog.setText(sb.toString());
        messageLog.setCaretPosition(messageLog.getDocument().getLength());

        mapCanvas.repaint();
    }

    /** Updates the turn-phase indicator and focuses the command field. */
    public void setPhase(boolean isP1Turn, String playerName) {
        Color c = isP1Turn ? P1_COLOR : P2_COLOR;
        phaseLabel.setText(playerName + "'s turn  ");
        phaseLabel.setForeground(c);
        commandPrompt.setText(playerName + "'s command:");
        commandPrompt.setForeground(c);

        p1Panel.setBorder(infoBorder(p1Name, P1_COLOR, isP1Turn));
        p2Panel.setBorder(infoBorder(p2Name, P2_COLOR, !isP1Turn));

        commandField.setText("");
        commandField.requestFocusInWindow();
    }

    // ── Internals ───────────────────────────────────────────────────

    private void submitCommand() {
        String text = commandField.getText().trim();
        commandField.setText("");
        fireCommand(text);
    }

    private void fireCommand(String text) {
        if (onCommand != null) onCommand.accept(text);
    }

    private static String formatInfo(PlayerView pv) {
        Map<String, String> a = pv.attributes();
        String loc   = a.getOrDefault("location", "?");
        String goods = GOODS.getOrDefault(loc, "?");
        return "Location:   " + loc + "  [" + goods + "]\n"
             + "Order:      " + a.getOrDefault("order", "NONE") + "\n"
             + "Inventory:  " + a.getOrDefault("inventory", "(empty)") + "\n"
             + "Deliveries: " + a.getOrDefault("deliveries", "0/2") + "\n"
             + "Profit:     " + a.getOrDefault("profit", "0g");
    }

    // ── Widget helpers ──────────────────────────────────────────────

    private static JLabel label(String text, Color fg, int size, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(fg);
        return l;
    }

    private static JTextArea infoArea() {
        JTextArea a = new JTextArea(6, 18);
        a.setEditable(false);
        a.setFont(new Font("SansSerif", Font.PLAIN, 12));
        a.setBackground(INFO_BG);
        a.setForeground(TEXT);
        a.setMargin(new Insets(6, 8, 6, 8));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private static JPanel infoPanel(JTextArea area, String name,
                                    Color color, boolean active) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(INFO_BG);
        p.setBorder(infoBorder(name, color, active));
        p.add(area, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(230, 200));
        return p;
    }

    private static TitledBorder infoBorder(String name, Color color, boolean active) {
        int thick = active ? 3 : 1;
        Color edge = active ? color : color.darker().darker();
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(edge, thick),
                name, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), color);
    }

    // ── Trade-map canvas (inner panel) ──────────────────────────────

    private class MapCanvas extends JPanel {
        MapCanvas() { setBackground(MAP_BG); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(DIM_TEXT);
            String title = "TRADE MAP";
            FontMetrics tfm = g2.getFontMetrics();
            g2.drawString(title, (getWidth() - tfm.stringWidth(title)) / 2, 22);

            // Routes
            g2.setColor(ROUTE_CLR);
            g2.setStroke(new BasicStroke(2.5f));
            for (String[] e : EDGES) {
                int[] a = NODE_POS.get(e[0]);
                int[] b = NODE_POS.get(e[1]);
                if (a != null && b != null) g2.drawLine(a[0], a[1], b[0], b[1]);
            }

            // Nodes
            g2.setStroke(new BasicStroke(1.5f));
            for (var entry : NODE_POS.entrySet()) {
                drawNode(g2, entry.getValue()[0], entry.getValue()[1],
                         entry.getKey(), GOODS.getOrDefault(entry.getKey(), ""));
            }

            // Player markers
            drawMarker(g2, p1Location, P1_COLOR, p1Name, -14);
            drawMarker(g2, p2Location, P2_COLOR, p2Name, 14);

            // Legend
            int ly = getHeight() - 20;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(P1_COLOR);
            g2.fillOval(60, ly - 7, 8, 8);
            g2.drawString(p1Name, 72, ly);
            g2.setColor(P2_COLOR);
            g2.fillOval(150, ly - 7, 8, 8);
            g2.drawString(p2Name, 162, ly);
        }

        private void drawNode(Graphics2D g, int cx, int cy,
                              String name, String goods) {
            int w = 114, h = 44;
            int x = cx - w / 2, y = cy - h / 2;

            g.setColor(NODE_BG);
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setColor(NODE_EDGE);
            g.drawRoundRect(x, y, w, h, 10, 10);

            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(TEXT);
            g.drawString(name, cx - fm.stringWidth(name) / 2, cy - 2);

            g.setFont(new Font("SansSerif", Font.ITALIC, 10));
            fm = g.getFontMetrics();
            g.setColor(GOLD);
            String lbl = "[" + goods + "]";
            g.drawString(lbl, cx - fm.stringWidth(lbl) / 2, cy + 13);
        }

        private void drawMarker(Graphics2D g, String location, Color color,
                                String name, int xOff) {
            int[] pos = NODE_POS.get(location);
            if (pos == null) return;
            int cx = pos[0] + xOff, cy = pos[1] + 32;

            g.setColor(color);
            g.fillOval(cx - 7, cy - 7, 14, 14);
            g.setColor(color.darker());
            g.drawOval(cx - 7, cy - 7, 14, 14);

            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(color);
            g.drawString(name, cx - fm.stringWidth(name) / 2, cy + 16);
        }
    }
}
