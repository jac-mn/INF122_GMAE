package gmae.ui.gui;

import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.PlayerView;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders the 2D top-down Relic Hunt arena using Java2D.
 *
 * <p>All game state is derived from {@link AdventureState} snapshots pushed
 * by {@link GuiSession}. Relic positions are tracked internally: the initial
 * set mirrors {@code RelicHuntAdventure}'s fixed starting layout, and relics
 * are removed when a player's score increases at a matching position.</p>
 */
public class GamePanel extends JPanel {

    // ── Grid geometry ───────────────────────────────────────────────
    private static final int GRID_SIZE = 10;
    private static final int TILE_SIZE = 50;
    private static final int ARENA_PX  = GRID_SIZE * TILE_SIZE;   // 500
    private static final int ARENA_X   = 150;                     // left margin
    private static final int ARENA_Y   = 80;                      // below HUD
    static final int PANEL_W = 800;
    static final int PANEL_H = 650;

    // ── Mirrors RelicHuntAdventure's fixed starting relic positions ──
    private static final List<int[]> INITIAL_RELIC_COORDS = List.of(
            new int[]{2, 2},
            new int[]{8, 8},
            new int[]{5, 5},
            new int[]{2, 8}
    );

    // ── Palette ─────────────────────────────────────────────────────
    private static final Color BG_COLOR    = new Color(34, 85, 34);
    private static final Color GRID_COLOR  = new Color(60, 120, 60);
    private static final Color P1_COLOR    = new Color(50, 130, 240);
    private static final Color P2_COLOR    = new Color(230, 60, 60);
    private static final Color RELIC_COLOR = new Color(255, 215, 0);
    private static final Color HUD_BG     = new Color(40, 40, 40);
    private static final Color STATUS_BG  = new Color(50, 50, 50);
    private static final Color TEXT_DIM   = new Color(180, 180, 180);
    private static final Color LABEL_BG   = new Color(0, 0, 0, 140);

    // Character colors
    private static final Color SKIN       = new Color(255, 213, 170);
    private static final Color OUTLINE    = new Color(40, 40, 40);
    private static final Color BOOT_COLOR = new Color(80, 50, 30);
    private static final Color BELT_COLOR = new Color(139, 90, 43);

    // Treasure chest colors
    private static final Color WOOD       = new Color(139, 90, 43);
    private static final Color WOOD_DARK  = new Color(100, 65, 30);
    private static final Color WOOD_LIGHT = new Color(175, 120, 60);
    private static final Color GOLD       = new Color(255, 215, 0);
    private static final Color GOLD_DARK  = new Color(200, 165, 30);
    private static final Color GEM_CYAN   = new Color(80, 220, 255);
    private static final Color GEM_RED    = new Color(230, 50, 80);

    // ── State ───────────────────────────────────────────────────────
    private final String p1Name;
    private final String p2Name;

    private AdventureState state;
    private int elapsedMs;
    private final Set<String> remainingRelics = new LinkedHashSet<>();
    private int prevP1Score;
    private int prevP2Score;

    public GamePanel(String p1Name, String p2Name) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(BG_COLOR);
        resetRelics();
    }

    /** Resets relic tracking for a new round. */
    public void resetRelics() {
        remainingRelics.clear();
        for (int[] c : INITIAL_RELIC_COORDS) {
            remainingRelics.add(c[0] + "," + c[1]);
        }
        prevP1Score = 0;
        prevP2Score = 0;
    }

    /** Pushes a new state snapshot from the game tick. */
    public void updateState(AdventureState newState, int elapsedMs) {
        this.state     = newState;
        this.elapsedMs = elapsedMs;
        if (newState == null) return;

        PlayerView p1 = newState.players().get(PlayerId.P1);
        PlayerView p2 = newState.players().get(PlayerId.P2);

        if (p1 != null && p1.score() > prevP1Score) {
            String pos = p1.attributes().get("position");
            if (pos != null) remainingRelics.remove(pos);
            prevP1Score = p1.score();
        }
        if (p2 != null && p2.score() > prevP2Score) {
            String pos = p2.attributes().get("position");
            if (pos != null) remainingRelics.remove(pos);
            prevP2Score = p2.score();
        }
    }

    // ── Rendering ───────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        drawHud(g2);
        drawArena(g2);
        drawRelics(g2);
        if (state != null) drawPlayers(g2);
        drawStatusBar(g2);
    }

    // ── HUD ─────────────────────────────────────────────────────────

    private void drawHud(Graphics2D g) {
        g.setColor(HUD_BG);
        g.fillRect(0, 0, getWidth(), ARENA_Y);

        int p1Score = scoreFor(PlayerId.P1);
        int p2Score = scoreFor(PlayerId.P2);

        // P1 info — left (mini character icon)
        drawMiniCharacter(g, 30, 35, P1_COLOR);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString(p1Name + "  " + p1Score + "/2 relics", 48, 42);

        // P2 info — right (mini character icon)
        drawMiniCharacter(g, getWidth() - 270, 35, P2_COLOR);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString(p2Name + "  " + p2Score + "/2 relics",
                     getWidth() - 252, 42);

        // Elapsed time — center
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        int totalSecs = elapsedMs / 1000;
        String timeStr = String.format("Time  %d:%02d", totalSecs / 60, totalSecs % 60);
        FontMetrics fm = g.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(timeStr)) / 2;
        g.setColor(Color.WHITE);
        g.drawString(timeStr, tx, 42);

        // Controls hint
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(TEXT_DIM);
        String hint = "P1: WASD    P2: Arrow Keys    ESC: Quit";
        FontMetrics fms = g.getFontMetrics();
        g.drawString(hint, (getWidth() - fms.stringWidth(hint)) / 2, 68);
    }

    private void drawMiniCharacter(Graphics2D g, int cx, int cy, Color color) {
        g.setColor(color);
        g.fillRoundRect(cx - 6, cy - 2, 12, 10, 3, 3);
        g.setColor(SKIN);
        g.fillOval(cx - 5, cy - 11, 10, 10);
        g.setColor(color.brighter());
        g.fillRoundRect(cx - 6, cy - 13, 12, 5, 3, 3);
    }

    // ── Arena ────────────────────────────────────────────────────────

    private void drawArena(Graphics2D g) {
        g.setColor(BG_COLOR);
        g.fillRect(ARENA_X, ARENA_Y, ARENA_PX, ARENA_PX);

        g.setColor(GRID_COLOR);
        for (int i = 0; i <= GRID_SIZE; i++) {
            int x = ARENA_X + i * TILE_SIZE;
            int y = ARENA_Y + i * TILE_SIZE;
            g.drawLine(x, ARENA_Y, x, ARENA_Y + ARENA_PX);
            g.drawLine(ARENA_X, y, ARENA_X + ARENA_PX, y);
        }
    }

    // ── Treasure Chests (relics) ────────────────────────────────────

    private void drawRelics(Graphics2D g) {
        for (String key : remainingRelics) {
            int[] pos = parseCsv(key);
            if (pos == null) continue;

            int cx = ARENA_X + pos[0] * TILE_SIZE + TILE_SIZE / 2;
            int cy = ARENA_Y + pos[1] * TILE_SIZE + TILE_SIZE / 2;
            drawTreasureChest(g, cx, cy);
        }
    }

    private void drawTreasureChest(Graphics2D g, int cx, int cy) {
        Stroke orig = g.getStroke();

        // Shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(cx - 14, cy + 8, 28, 8);

        // ── Chest body ──
        g.setColor(WOOD);
        g.fillRoundRect(cx - 13, cy - 1, 26, 15, 4, 4);
        g.setColor(WOOD_DARK);
        g.drawRoundRect(cx - 13, cy - 1, 26, 15, 4, 4);

        // Horizontal bands
        g.setColor(GOLD_DARK);
        g.fillRect(cx - 13, cy + 2, 26, 2);
        g.fillRect(cx - 13, cy + 9, 26, 2);

        // Lock/clasp
        g.setColor(GOLD);
        g.fillRoundRect(cx - 3, cy + 4, 6, 5, 2, 2);
        g.setColor(GOLD_DARK);
        g.drawRoundRect(cx - 3, cy + 4, 6, 5, 2, 2);
        g.setColor(OUTLINE);
        g.fillOval(cx - 1, cy + 5, 2, 2);

        // ── Open lid ──
        g.setColor(WOOD_LIGHT);
        g.fillArc(cx - 13, cy - 14, 26, 18, 0, 180);
        g.setColor(WOOD);
        g.fillRect(cx - 13, cy - 5, 26, 5);
        g.setColor(WOOD_DARK);
        g.drawArc(cx - 13, cy - 14, 26, 18, 0, 180);
        g.drawLine(cx - 13, cy - 5, cx + 13, cy - 5);

        // Lid band
        g.setColor(GOLD_DARK);
        g.fillRect(cx - 13, cy - 7, 26, 2);

        // Lid vertical clasp
        g.setColor(GOLD_DARK);
        g.fillRect(cx - 1, cy - 13, 2, 8);

        // ── Treasure inside (visible above chest rim) ──
        // Gold coins
        g.setColor(GOLD);
        g.fillOval(cx - 8, cy - 5, 7, 6);
        g.fillOval(cx - 3, cy - 6, 7, 6);
        g.fillOval(cx + 3, cy - 4, 7, 6);
        g.setColor(GOLD_DARK);
        g.drawOval(cx - 8, cy - 5, 7, 6);
        g.drawOval(cx - 3, cy - 6, 7, 6);
        g.drawOval(cx + 3, cy - 4, 7, 6);

        // Coin detail (inner circles)
        g.setColor(new Color(255, 235, 100));
        g.fillOval(cx - 6, cy - 3, 3, 3);
        g.fillOval(cx - 1, cy - 4, 3, 3);
        g.fillOval(cx + 5, cy - 2, 3, 3);

        // Red gem
        Polygon gem1 = new Polygon(
                new int[]{cx - 5, cx - 2, cx - 5, cx - 8},
                new int[]{cy - 11, cy - 8, cy - 5, cy - 8}, 4);
        g.setColor(GEM_RED);
        g.fill(gem1);
        g.setColor(GEM_RED.darker());
        g.draw(gem1);

        // Cyan gem
        Polygon gem2 = new Polygon(
                new int[]{cx + 5, cx + 8, cx + 5, cx + 2},
                new int[]{cy - 12, cy - 9, cy - 6, cy - 9}, 4);
        g.setColor(GEM_CYAN);
        g.fill(gem2);
        g.setColor(GEM_CYAN.darker());
        g.draw(gem2);

        // Sparkles
        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(1.5f));
        drawSparkle(g, cx + 1, cy - 13, 3);
        drawSparkle(g, cx - 10, cy - 8, 2);
        drawSparkle(g, cx + 10, cy - 7, 2);
        g.setStroke(orig);
    }

    private void drawSparkle(Graphics2D g, int x, int y, int size) {
        g.drawLine(x - size, y, x + size, y);
        g.drawLine(x, y - size, x, y + size);
    }

    // ── Player Characters ───────────────────────────────────────────

    private void drawPlayers(Graphics2D g) {
        PlayerView p1 = state.players().get(PlayerId.P1);
        PlayerView p2 = state.players().get(PlayerId.P2);
        int[] p1Pos = parsePosition(p1);
        int[] p2Pos = parsePosition(p2);

        boolean sameCell = p1Pos != null && p2Pos != null
                && p1Pos[0] == p2Pos[0] && p1Pos[1] == p2Pos[1];

        if (p1Pos != null) drawCharacter(g, p1Pos, P1_COLOR, p1Name, sameCell ? -10 : 0);
        if (p2Pos != null) drawCharacter(g, p2Pos, P2_COLOR, p2Name, sameCell ?  10 : 0);
    }

    private void drawCharacter(Graphics2D g, int[] pos, Color color,
                               String name, int xOffset) {
        int cx = ARENA_X + pos[0] * TILE_SIZE + TILE_SIZE / 2 + xOffset;
        int cy = ARENA_Y + pos[1] * TILE_SIZE + TILE_SIZE / 2 + 2;

        // Shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(cx - 10, cy + 12, 20, 7);

        // ── Boots ──
        g.setColor(BOOT_COLOR);
        g.fillRoundRect(cx - 8, cy + 10, 7, 5, 2, 2);
        g.fillRoundRect(cx + 1, cy + 10, 7, 5, 2, 2);

        // ── Legs ──
        g.setColor(color.darker().darker());
        g.fillRect(cx - 6, cy + 5, 5, 6);
        g.fillRect(cx + 1, cy + 5, 5, 6);

        // ── Body / Tunic ──
        g.setColor(color);
        g.fillRoundRect(cx - 8, cy - 5, 16, 12, 4, 4);
        g.setColor(OUTLINE);
        g.drawRoundRect(cx - 8, cy - 5, 16, 12, 4, 4);

        // Belt
        g.setColor(BELT_COLOR);
        g.fillRect(cx - 8, cy + 2, 16, 3);
        g.setColor(GOLD_DARK);
        g.fillRect(cx - 2, cy + 2, 4, 3);

        // ── Arms ──
        g.setColor(color);
        g.fillRoundRect(cx - 13, cy - 3, 6, 12, 3, 3);
        g.fillRoundRect(cx + 7, cy - 3, 6, 12, 3, 3);
        g.setColor(OUTLINE);
        g.drawRoundRect(cx - 13, cy - 3, 6, 12, 3, 3);
        g.drawRoundRect(cx + 7, cy - 3, 6, 12, 3, 3);

        // Hands
        g.setColor(SKIN);
        g.fillOval(cx - 12, cy + 7, 5, 5);
        g.fillOval(cx + 8, cy + 7, 5, 5);

        // ── Head ──
        g.setColor(SKIN);
        g.fillOval(cx - 8, cy - 18, 16, 15);
        g.setColor(OUTLINE);
        g.drawOval(cx - 8, cy - 18, 16, 15);

        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(cx - 5, cy - 14, 5, 5);
        g.fillOval(cx + 1, cy - 14, 5, 5);
        g.setColor(new Color(40, 40, 40));
        g.fillOval(cx - 3, cy - 13, 3, 3);
        g.fillOval(cx + 2, cy - 13, 3, 3);

        // Mouth
        g.setColor(new Color(180, 100, 80));
        g.drawArc(cx - 3, cy - 8, 6, 3, 200, 140);

        // ── Helmet ──
        g.setColor(color.brighter());
        g.fillArc(cx - 9, cy - 23, 18, 12, 0, 180);
        g.setColor(OUTLINE);
        g.drawArc(cx - 9, cy - 23, 18, 12, 0, 180);

        // Helmet visor band
        g.setColor(color.darker());
        g.fillRect(cx - 9, cy - 17, 18, 2);

        // Helmet crest (small triangle on top)
        g.setColor(RELIC_COLOR);
        Polygon crest = new Polygon(
                new int[]{cx, cx + 3, cx - 3},
                new int[]{cy - 26, cy - 22, cy - 22}, 3);
        g.fill(crest);

        // ── Name label ──
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(name);
        int tx = cx - tw / 2;
        int ty = cy - 29;

        g.setColor(LABEL_BG);
        g.fillRoundRect(tx - 3, ty - fm.getAscent(),
                        tw + 6, fm.getHeight(), 4, 4);
        g.setColor(color);
        g.drawString(name, tx, ty);
    }

    // ── Status Bar ──────────────────────────────────────────────────

    private void drawStatusBar(Graphics2D g) {
        int barY = ARENA_Y + ARENA_PX + 5;
        g.setColor(STATUS_BG);
        g.fillRect(0, barY, getWidth(), getHeight() - barY);

        if (state == null) return;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(220, 220, 220));

        List<String> msgs = state.messages().stream()
                .filter(m -> !m.contains("passes"))
                .toList();
        int maxLines = 3;
        int start = Math.max(0, msgs.size() - maxLines);
        int y = barY + 18;
        for (int i = start; i < msgs.size() && i < start + maxLines; i++) {
            g.drawString("  " + msgs.get(i), 10, y);
            y += 18;
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private int scoreFor(PlayerId id) {
        if (state == null) return 0;
        PlayerView v = state.players().get(id);
        return v != null ? v.score() : 0;
    }

    private static int[] parsePosition(PlayerView view) {
        if (view == null) return null;
        return parseCsv(view.attributes().get("position"));
    }

    private static int[] parseCsv(String csv) {
        if (csv == null || !csv.contains(",")) return null;
        try {
            String[] p = csv.split(",");
            return new int[]{Integer.parseInt(p[0].trim()),
                             Integer.parseInt(p[1].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
