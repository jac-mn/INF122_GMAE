package gmae.core.engine;

import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Outcome;
import gmae.core.model.PlayerView;

import java.util.Scanner;

/**
 * Simple turn-based game loop.
 *
 * <p>Each iteration prompts P1 then P2 for input, calls {@code advance()},
 * prints the resulting state, and checks for completion.</p>
 */
public class GameLoop {

    private final AdventureManager manager;
    private final InputRouter inputRouter;
    private final Scanner scanner;

    public GameLoop(AdventureManager manager, InputRouter inputRouter, Scanner scanner) {
        this.manager = manager;
        this.inputRouter = inputRouter;
        this.scanner = scanner;
    }

    /**
     * Runs the adventure identified by {@code adventureId} with the given
     * player names, including an optional replay loop.
     */
    public void run(String adventureId, String p1Name, String p2Name) {
        boolean playing = true;
        boolean firstRun = true;

        while (playing) {
            AdventureState state = firstRun
                    ? manager.start(adventureId)
                    : manager.restart();
            firstRun = false;

            printState(state, p1Name, p2Name);

            while (!manager.isFinished()) {
                System.out.printf("%n  [%s] Enter action: ", p1Name);
                manager.submitInput(PlayerId.P1, inputRouter.parse(scanner.nextLine()));

                System.out.printf("  [%s] Enter action: ", p2Name);
                manager.submitInput(PlayerId.P2, inputRouter.parse(scanner.nextLine()));

                state = manager.step();
                printState(state, p1Name, p2Name);
            }

            printOutcome(state, p1Name, p2Name);

            System.out.print("\n  Play again? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            playing = answer.equals("y") || answer.equals("yes");
        }
    }

    // ── display helpers ────────────────────────────────────────────

    private void printState(AdventureState state, String p1Name, String p2Name) {
        System.out.println();
        System.out.println("  ── " + state.title() + " | Turn " + state.tickOrTurn() + " ──");

        if (!state.messages().isEmpty()) {
            for (String msg : state.messages()) {
                System.out.println("    > " + msg);
            }
        }

        PlayerView p1 = state.players().get(PlayerId.P1);
        PlayerView p2 = state.players().get(PlayerId.P2);
        if (p1 != null) printPlayer(p1, p1Name);
        if (p2 != null) printPlayer(p2, p2Name);

        System.out.println("    Status: " + state.outcome());
    }

    private void printPlayer(PlayerView view, String displayName) {
        StringBuilder sb = new StringBuilder();
        sb.append("    ").append(displayName)
          .append(" (").append(view.id()).append(")")
          .append("  Score: ").append(view.score());

        if (!view.attributes().isEmpty()) {
            view.attributes().forEach((k, v) ->
                    sb.append("  ").append(k).append("=").append(v));
        }
        System.out.println(sb);
    }

    private void printOutcome(AdventureState state, String p1Name, String p2Name) {
        System.out.println();
        Outcome outcome = state.outcome();
        String result = switch (outcome) {
            case P1_WINS  -> p1Name + " wins!";
            case P2_WINS  -> p2Name + " wins!";
            case DRAW     -> "It's a draw!";
            case COOP_WIN -> "Both players win!";
            case LOSS     -> "Both players lose!";
            default       -> outcome.toString();
        };
        System.out.println("  ★  RESULT: " + result);
    }
}
