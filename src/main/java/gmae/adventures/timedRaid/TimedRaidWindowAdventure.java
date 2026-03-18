package gmae.adventures.timedRaid;

import gmae.core.api.InputEvent;
import gmae.core.api.MiniAdventure;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.Outcome;
import gmae.core.model.PlayerView;
import gmae.core.model.QuestEventView;
import gmae.core.services.GmaeQuestEventService;
import gmae.core.services.GmaeRealmService;
import gmae.core.services.GmaeTimeService;
import gmae.core.services.ServiceBundle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Time-based cooperative raid adventure.
 *
 * <p>Two players work together to complete raid objectives within a time limit.
 * Uses {@link GmaeRealmService} for movement, {@link GmaeTimeService} for
 * countdown, and {@link GmaeQuestEventService} to track objective completion.</p>
 *
 * <p><strong>Actions:</strong></p>
 * <ul>
 *   <li>{@code MOVE NORTH/SOUTH/EAST/WEST} — move around the map</li>
 *   <li>{@code COMPLETE} — complete the objective at current location</li>
 *   <li>Any other action is silently ignored</li>
 * </ul>
 *
 * <p><strong>Win Condition:</strong> Complete all 3 objectives before
 * 12 in-game minutes expire.</p>
 */
public class TimedRaidWindowAdventure implements MiniAdventure {

    private static final String REALM = "Shadowfen";
    private static final int TIME_LIMIT_MINUTES = 12;
    private static final int MINUTES_PER_TURN = 2;
    private static final int TOTAL_OBJECTIVES = 3;

    private GmaeRealmService realm;
    private GmaeTimeService time;
    private GmaeQuestEventService quests;

    private int turn;
    private long startTime;
    private int objectivesCompleted;
    private Outcome outcome;
    private final List<String> messages = new ArrayList<>();
    private final Map<PlayerId, InputEvent> inputBuffer = new EnumMap<>(PlayerId.class);
    private final Map<String, Coord> objectiveLocations = new HashMap<>();
    private final Map<String, String> objectiveIds = new HashMap<>();  // quest event IDs

    @Override public String id()          { return "timed-raid"; }
    @Override public String title()       { return "Timed Raid"; }
    @Override public String description() { return "Complete raid objectives cooperatively before time expires!"; }

    @Override
    public void init() {
        turn = 0;
        objectivesCompleted = 0;
        outcome = Outcome.IN_PROGRESS;
        messages.clear();
        inputBuffer.clear();
        objectiveLocations.clear();
        objectiveIds.clear();

        // Place 3 raid objectives at different coordinates
        objectiveLocations.put("obj-1", new Coord(2, 2));
        objectiveLocations.put("obj-2", new Coord(8, 8));
        objectiveLocations.put("obj-3", new Coord(5, 2));

        messages.add("Welcome to the Timed Raid!");
        messages.add("Complete all objectives before " + TIME_LIMIT_MINUTES + " minutes!");
        messages.add("Actions: MOVE NORTH|SOUTH|EAST|WEST, COMPLETE");
        messages.add("Objective Locations: (2,2), (8,8), (5,2)");
    }

    @Override
    public void bindServices(ServiceBundle services) {
        this.realm = services.realmService().orElse(null);
        this.time = services.timeService().orElse(null);
        this.quests = services.questEventService().orElse(null);

        if (this.realm != null) {
            try {
                // Place both players at starting positions
                this.realm.placePlayer(PlayerId.P1, REALM, new Coord(0, 0));
                this.realm.placePlayer(PlayerId.P2, REALM, new Coord(1, 0));

                // Place objective entities in the realm
                int objIdx = 1;
                for (Map.Entry<String, Coord> entry : objectiveLocations.entrySet()) {
                    EntityView objective = new EntityView(
                            entry.getKey(),
                            "Objective " + objIdx,
                            "objective",
                            entry.getValue(),
                            REALM
                    );
                    this.realm.addEntity(objective);
                    
                    // Create quest event for this objective if questService available
                    if (this.quests != null) {
                        QuestEventView quest = new QuestEventView(
                                UUID.randomUUID().toString(),
                                "Objective " + objIdx,
                                0,
                                TIME_LIMIT_MINUTES * 60,
                                REALM
                        );
                        String questId = this.quests.addEvent(quest);
                        objectiveIds.put(entry.getKey(), questId);
                    }
                    objIdx++;
                }

                messages.add("Both players placed. Objective markers visible.");
                if (this.time != null) {
                    startTime = this.time.nowWorld().toTotalMinutes();
                }
            } catch (Exception e) {
                messages.add("Service error: " + e.getMessage());
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
        
        long elapsed = 0;
        if (this.time != null) {
            elapsed = this.time.nowWorld().toTotalMinutes() - startTime;
        }
        
        messages.add("═══════════════════════════════════════");
        messages.add("Turn " + turn + " | Objectives: " + objectivesCompleted + "/" + TOTAL_OBJECTIVES + 
                    " | Time: " + elapsed + "/" + TIME_LIMIT_MINUTES + " min");
        
        if (realm != null) {
            Optional<Coord> p1Pos = realm.playerPosition(PlayerId.P1);
            Optional<Coord> p2Pos = realm.playerPosition(PlayerId.P2);
            messages.add("P1 @ " + p1Pos.map(c -> "(" + c.x() + "," + c.y() + ")").orElse("?") + 
                        "    P2 @ " + p2Pos.map(c -> "(" + c.x() + "," + c.y() + ")").orElse("?"));
        }
        messages.add("─────────────────────────────────────");

        // Advance time
        if (this.time != null) {
            this.time.advanceMinutes(MINUTES_PER_TURN);
        }

        // Process player actions
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

        p1Attrs.put("objectives", objectivesCompleted + "/" + TOTAL_OBJECTIVES);
        p2Attrs.put("objectives", objectivesCompleted + "/" + TOTAL_OBJECTIVES);

        players.put(PlayerId.P1, new PlayerView(PlayerId.P1, "P1", objectivesCompleted, p1Attrs));
        players.put(PlayerId.P2, new PlayerView(PlayerId.P2, "P2", objectivesCompleted, p2Attrs));

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
            messages.add(player + " waits.");
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
            } else {
                messages.add(player + " ERROR: Cannot move " + payload.toUpperCase() + " (blocked/boundary)");
            }
        } else if ("COMPLETE".equalsIgnoreCase(action)) {
            completeObjectiveAtPosition(player);
        } else {
            messages.add(player + " ERROR: Unknown action '" + action + "' (try MOVE DIRECTION or COMPLETE)");
        }
    }

    private void completeObjectiveAtPosition(PlayerId player) {
        if (realm == null) return;

        Optional<Coord> pos = realm.playerPosition(player);
        if (pos.isEmpty()) {
            messages.add(player + " ERROR: Not placed on map!");
            return;
        }

        Coord playerCoord = pos.get();
        boolean found = false;
        for (Map.Entry<String, Coord> entry : objectiveLocations.entrySet()) {
            if (entry.getValue().equals(playerCoord)) {
                String objId = entry.getKey();
                if (realm.removeEntity(objId)) {
                    objectivesCompleted++;
                    messages.add("  *** " + player + " COMPLETED OBJECTIVE! (" + objectivesCompleted + 
                                "/" + TOTAL_OBJECTIVES + ") ***");
                    objectiveLocations.remove(objId);
                    found = true;
                }
            }
        }
        
        if (!found) {
            messages.add(player + " ERROR: No objective at current location " + 
                        "(" + playerCoord.x() + "," + playerCoord.y() + ")");
        }
    }

    private void checkWinCondition() {
        if (objectivesCompleted >= TOTAL_OBJECTIVES) {
            outcome = Outcome.COOP_WIN;
            messages.add("═════════════════════════════════════");
            messages.add("RAID SUCCESS: All objectives complete!");
            messages.add("RESULT: VICTORY!");
        } else if (time != null) {
            long elapsed = time.nowWorld().toTotalMinutes() - startTime;
            if (elapsed >= TIME_LIMIT_MINUTES) {
                outcome = Outcome.LOSS;
                messages.add("═════════════════════════════════════");
                messages.add("RAID FAILED: Time expired!");
                messages.add("RESULT: FAILURE (" + objectivesCompleted + "/" + TOTAL_OBJECTIVES + " objectives complete)");
            }
        }
    }
}
