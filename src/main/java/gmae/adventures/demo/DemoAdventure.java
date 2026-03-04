package gmae.adventures.demo;

import gmae.core.api.InputEvent;
import gmae.core.api.MiniAdventure;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Outcome;
import gmae.core.model.PlayerView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A trivial 3-turn competitive adventure used to validate the engine
 * end-to-end.
 *
 * <p>Each turn both players choose one of: <b>EXPLORE</b> (+2 pts),
 * <b>REST</b> (+1 pt), or anything else / <b>PASS</b> (+0 pts).
 * After three turns the player with the higher score wins.</p>
 *
 * <p><strong>Import rule:</strong> this class deliberately imports only
 * from {@code gmae.core.api} and {@code gmae.core.model}.</p>
 */
public class DemoAdventure implements MiniAdventure {

    private static final int MAX_TURNS = 3;

    private int turn;
    private int p1Score;
    private int p2Score;
    private Outcome outcome;
    private final List<String> messages = new ArrayList<>();
    private final Map<PlayerId, InputEvent> inputBuffer = new EnumMap<>(PlayerId.class);

    // ── metadata (safe to call at any time) ─────────────────────

    @Override public String id()          { return "demo"; }
    @Override public String title()       { return "Demo Adventure"; }
    @Override public String description() { return "A 3-turn scoring game — EXPLORE, REST, or PASS each turn."; }

    // ── lifecycle ───────────────────────────────────────────────

    @Override
    public void init() {
        turn = 0;
        p1Score = 0;
        p2Score = 0;
        outcome = Outcome.IN_PROGRESS;
        messages.clear();
        inputBuffer.clear();
        messages.add("Welcome! Each turn choose: EXPLORE (+2), REST (+1), or PASS (+0).");
        messages.add("The player with the highest score after " + MAX_TURNS + " turns wins.");
    }

    @Override
    public void acceptInput(PlayerId player, InputEvent event) {
        inputBuffer.put(player, event);
    }

    @Override
    public void advance() {
        turn++;
        messages.clear();
        messages.add("── Turn " + turn + " of " + MAX_TURNS + " ──");

        processPlayer(PlayerId.P1);
        processPlayer(PlayerId.P2);
        inputBuffer.clear();

        if (turn >= MAX_TURNS) {
            if (p1Score > p2Score)      outcome = Outcome.P1_WINS;
            else if (p2Score > p1Score) outcome = Outcome.P2_WINS;
            else                        outcome = Outcome.DRAW;
            messages.add("Final scores — P1: " + p1Score + "  P2: " + p2Score);
        }
    }

    @Override
    public AdventureState reportState() {
        Map<PlayerId, PlayerView> players = new EnumMap<>(PlayerId.class);
        players.put(PlayerId.P1, new PlayerView(PlayerId.P1, "P1", p1Score));
        players.put(PlayerId.P2, new PlayerView(PlayerId.P2, "P2", p2Score));

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

    // ── internals ──────────────────────────────────────────────

    private void processPlayer(PlayerId player) {
        InputEvent event = inputBuffer.get(player);
        String action = (event != null) ? event.action() : "PASS";

        int points;
        String verb;
        switch (action.toUpperCase()) {
            case "EXPLORE" -> { points = 2; verb = "explores and finds treasure (+2)"; }
            case "REST"    -> { points = 1; verb = "rests and recovers (+1)"; }
            default        -> { points = 0; verb = "passes (+0)"; }
        }

        if (player == PlayerId.P1) p1Score += points;
        else                       p2Score += points;

        messages.add(player + " " + verb);
    }
}
