package gmae.adventures.relicHunt;

import gmae.core.api.InputEvent;
import gmae.core.api.MiniAdventure;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.Outcome;
import gmae.core.model.PlayerView;
import gmae.core.services.GmaeRealmService;
import gmae.core.services.ServiceBundle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Realm-based treasure hunt adventure.
 *
 * <p>Two players move around a 10×10 "Shadowfen" map collecting relics.
 * The first player to collect 2 relics wins. Uses {@link GmaeRealmService}
 * for movement and entity tracking.</p>
 *
 * <p><strong>Actions:</strong></p>
 * <ul>
 *   <li>{@code MOVE NORTH} — move up</li>
 *   <li>{@code MOVE SOUTH} — move down</li>
 *   <li>{@code MOVE EAST} — move right</li>
 *   <li>{@code MOVE WEST} — move left</li>
 *   <li>Any other action is silently ignored</li>
 * </ul>
 *
 * <p><strong>Winning:</strong> First player to collect 2 relics wins.
 * If both collect all 3, it's a draw.</p>
 */
public class RelicHuntAdventure implements MiniAdventure {

    private static final String REALM = "Shadowfen";
    private static final int RELICS_TO_WIN = 2;
    private static final int TOTAL_RELICS = 3;

    private GmaeRealmService realm;
    private int turn;
    private int p1Relics;
    private int p2Relics;
    private Outcome outcome;
    private final List<String> messages = new ArrayList<>();
    private final Map<PlayerId, InputEvent> inputBuffer = new EnumMap<>(PlayerId.class);
    private final Map<String, Coord> relicPositions = new HashMap<>();

    @Override public String id()          { return "relic-hunt"; }
    @Override public String title()       { return "Relic Hunt"; }
    @Override public String description() { return "Collect relics across Shadowfen — first to 2 wins!"; }

    @Override
    public void init() {
        turn = 0;
        p1Relics = 0;
        p2Relics = 0;
        outcome = Outcome.IN_PROGRESS;
        messages.clear();
        inputBuffer.clear();
        relicPositions.clear();

        // Initialize relics at fixed positions
        relicPositions.put("relic-1", new Coord(2, 2));
        relicPositions.put("relic-2", new Coord(8, 8));
        relicPositions.put("relic-3", new Coord(5, 5));

        messages.add("Welcome to Relic Hunt!");
        messages.add("Collect " + RELICS_TO_WIN + " relics to win.");
        messages.add("Actions: MOVE NORTH|SOUTH|EAST|WEST");
        messages.add("Relic Locations: (2,2), (8,8), (5,5)");
    }

    @Override
    public void bindServices(ServiceBundle services) {
        this.realm = services.realmService().orElse(null);

        if (this.realm != null) {
            // Place both players at the starting corner
            try {
                this.realm.placePlayer(PlayerId.P1, REALM, new Coord(0, 0));
                this.realm.placePlayer(PlayerId.P2, REALM, new Coord(9, 9));
                
                // Place relics as entities in the realm
                for (Map.Entry<String, Coord> entry : relicPositions.entrySet()) {
                    EntityView relic = new EntityView(
                            entry.getKey(),
                            "Relic " + entry.getValue(),
                            "treasure",
                            entry.getValue(),
                            REALM
                    );
                    this.realm.addEntity(relic);
                }
                messages.add("Both players placed. Relics scattered!");
            } catch (Exception e) {
                messages.add("Realm service error: " + e.getMessage());
            }
        }
    }

    @Override
    public void acceptInput(PlayerId player, InputEvent event) {
        inputBuffer.put(player, event);
    }

    @Override
    public void advance() {
        turn++;
        messages.clear();
        messages.add("═══════════════════════════════════════");
        messages.add("Turn " + turn + " | P1: " + p1Relics + "/" + RELICS_TO_WIN + 
                    " relics  |  P2: " + p2Relics + "/" + RELICS_TO_WIN + " relics");
        
        if (realm != null) {
            Optional<Coord> p1Pos = realm.playerPosition(PlayerId.P1);
            Optional<Coord> p2Pos = realm.playerPosition(PlayerId.P2);
            messages.add("P1 @ " + p1Pos.map(c -> "(" + c.x() + "," + c.y() + ")").orElse("?") + 
                        "    P2 @ " + p2Pos.map(c -> "(" + c.x() + "," + c.y() + ")").orElse("?"));
        }
        messages.add("─────────────────────────────────────");

        processPlayer(PlayerId.P1);
        processPlayer(PlayerId.P2);
        inputBuffer.clear();

        checkWinCondition();
    }

    @Override
    public AdventureState reportState() {
        Map<PlayerId, PlayerView> players = new EnumMap<>(PlayerId.class);
        Map<String, String> p1Attrs = new HashMap<>();
        Map<String, String> p2Attrs = new HashMap<>();

        if (realm != null) {
            Optional<Coord> p1Pos = realm.playerPosition(PlayerId.P1);
            Optional<Coord> p2Pos = realm.playerPosition(PlayerId.P2);
            p1Attrs.put("position", p1Pos.map(c -> c.x() + "," + c.y()).orElse("?"));
            p2Attrs.put("position", p2Pos.map(c -> c.x() + "," + c.y()).orElse("?"));
        }
        p1Attrs.put("relics", String.valueOf(p1Relics));
        p2Attrs.put("relics", String.valueOf(p2Relics));

        players.put(PlayerId.P1, new PlayerView(PlayerId.P1, "P1", p1Relics, p1Attrs));
        players.put(PlayerId.P2, new PlayerView(PlayerId.P2, "P2", p2Relics, p2Attrs));

        return AdventureState.builder()
                .title(title())
                .tickOrTurn(turn)
                .messages(List.copyOf(messages))
                .players(players)
                .complete(outcome != Outcome.IN_PROGRESS)
                .outcome(outcome)
                .build();
    }

    @Override
    public boolean isComplete() {
        return outcome != Outcome.IN_PROGRESS;
    }

    @Override
    public void reset() {
        init();
    }

    // ── internals ──────────────────────────────────────────

    private void processPlayer(PlayerId player) {
        if (realm == null) return;

        InputEvent event = inputBuffer.get(player);
        if (event == null) {
            messages.add(player + " passes.");
            return;
        }
        
        String action = event.action();
        String payload = event.payload();

        if ("MOVE".equalsIgnoreCase(action)) {
            if (payload == null || payload.trim().isEmpty()) {
                messages.add(player + " ERROR: MOVE requires NORTH|SOUTH|EAST|WEST");
                return;
            }
            
            int dx = 0, dy = 0;
            switch (payload.toUpperCase()) {
                case "NORTH" -> dy = -1;
                case "SOUTH" -> dy = 1;
                case "EAST"  -> dx = 1;
                case "WEST"  -> dx = -1;
                default      -> {
                    messages.add(player + " ERROR: Invalid direction '" + payload + "' (try NORTH|SOUTH|EAST|WEST)");
                    return;
                }
            }

            if (realm.movePlayer(player, dx, dy)) {
                Optional<Coord> newPos = realm.playerPosition(player);
                messages.add(player + " moves " + payload.toUpperCase() + 
                           " to " + newPos.map(c -> "(" + c.x() + "," + c.y() + ")").orElse("?"));
                checkRelicAtPosition(player);
            } else {
                messages.add(player + " ERROR: Cannot move " + payload.toUpperCase() + " (blocked/boundary)");
            }
        } else {
            messages.add(player + " ERROR: Unknown action '" + action + "' (try MOVE DIRECTION)");
        }
    }

    private void checkRelicAtPosition(PlayerId player) {
        if (realm == null) return;

        Optional<Coord> pos = realm.playerPosition(player);
        if (pos.isEmpty()) return;

        Coord playerCoord = pos.get();
        for (Map.Entry<String, Coord> entry : relicPositions.entrySet()) {
            if (entry.getValue().equals(playerCoord)) {
                String relicId = entry.getKey();
                if (realm.removeEntity(relicId)) {
                    if (player == PlayerId.P1) {
                        p1Relics++;
                        messages.add("  *** P1 COLLECTED A RELIC! (" + p1Relics + "/" + RELICS_TO_WIN + ") ***");
                    } else {
                        p2Relics++;
                        messages.add("  *** P2 COLLECTED A RELIC! (" + p2Relics + "/" + RELICS_TO_WIN + ") ***");
                    }
                    relicPositions.remove(relicId);
                }
            }
        }
    }

    private void checkWinCondition() {
        if (p1Relics >= RELICS_TO_WIN && p2Relics >= RELICS_TO_WIN) {
            outcome = Outcome.DRAW;
            messages.add("═════════════════════════════════════");
            messages.add("GAME END: Both collected " + RELICS_TO_WIN + " relics!");
            messages.add("RESULT: DRAW!");
        } else if (p1Relics >= RELICS_TO_WIN) {
            outcome = Outcome.P1_WINS;
            messages.add("═════════════════════════════════════");
            messages.add("GAME END: P1 collected " + RELICS_TO_WIN + " relics!");
            messages.add("RESULT: P1 WINS!");
        } else if (p2Relics >= RELICS_TO_WIN) {
            outcome = Outcome.P2_WINS;
            messages.add("═════════════════════════════════════");
            messages.add("GAME END: P2 collected " + RELICS_TO_WIN + " relics!");
            messages.add("RESULT: P2 WINS!");
        }
    }
}
