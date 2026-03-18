package gmae;

import gmae.adventures.demo.DemoAdventure;
import gmae.adventures.relicHunt.RelicHuntAdventure;
import gmae.adventures.timedRaid.TimedRaidWindowAdventure;
import gmae.core.engine.AdventureRegistry;
import gmae.ui.ConsoleUI;

import java.util.Scanner;

/**
 * Entry point for the GMAE runtime.
 *
 * <p>Registers all known adventures, then launches the console UI.</p>
 */
public class Main {

    public static void main(String[] args) {
        AdventureRegistry registry = new AdventureRegistry();
        registry.register(DemoAdventure::new);
        registry.register(RelicHuntAdventure::new);
        registry.register(TimedRaidWindowAdventure::new);

        Scanner scanner = new Scanner(System.in);
        new ConsoleUI(registry, scanner).run();
        scanner.close();
    }
}
