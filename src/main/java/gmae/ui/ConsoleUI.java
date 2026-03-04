package gmae.ui;

import gmae.core.engine.AdventureManager;
import gmae.core.engine.AdventureRegistry;
import gmae.core.engine.GameLoop;
import gmae.core.engine.InputRouter;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based front-end for GMAE.
 *
 * <p>Shows an adventure menu, collects player names, and delegates gameplay
 * to {@link GameLoop}.</p>
 */
public class ConsoleUI {

    private final AdventureRegistry registry;
    private final Scanner scanner;

    public ConsoleUI(AdventureRegistry registry, Scanner scanner) {
        this.registry = registry;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("========================================");
        System.out.println("  GMAE — Mini-Adventure Environment");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Play an Adventure");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> playAdventure();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }

        System.out.println("\nGoodbye!");
    }

    private void playAdventure() {
        List<AdventureRegistry.EntryInfo> adventures = registry.listAdventures();
        if (adventures.isEmpty()) {
            System.out.println("No adventures registered.");
            return;
        }

        System.out.println("\n--- AVAILABLE ADVENTURES ---");
        for (int i = 0; i < adventures.size(); i++) {
            AdventureRegistry.EntryInfo e = adventures.get(i);
            System.out.printf("  %d. %-20s — %s%n", i + 1, e.title(), e.description());
        }
        System.out.print("Select adventure (number): ");

        int index;
        try {
            index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (index < 0 || index >= adventures.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        String adventureId = adventures.get(index).id();

        System.out.print("\nPlayer 1 name: ");
        String p1Name = scanner.nextLine().trim();
        if (p1Name.isEmpty()) p1Name = "Player 1";

        System.out.print("Player 2 name: ");
        String p2Name = scanner.nextLine().trim();
        if (p2Name.isEmpty()) p2Name = "Player 2";

        AdventureManager manager = new AdventureManager(registry);
        InputRouter router = new InputRouter();
        GameLoop loop = new GameLoop(manager, router, scanner);
        loop.run(adventureId, p1Name, p2Name);
    }
}
