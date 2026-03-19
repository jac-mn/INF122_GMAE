package gmae;

import gmae.adapters.InventoryAdapter;
import gmae.adventures.demo.DemoAdventure;
import gmae.core.engine.AdventureRegistry;
import gmae.core.services.ServiceBundle;
import gmae.ui.ConsoleUI;

import java.util.Scanner;

/**
 * Entry point for the GMAE runtime.
 *
 * <p>Registers all known adventures, builds a {@link ServiceBundle}
 * with real adapters wrapping legacy GuildQuest subsystems, then
 * launches the console UI.</p>
 */
public class Main {

    public static void main(String[] args) {
        AdventureRegistry registry = new AdventureRegistry();
        registry.register(DemoAdventure::new);

        // Wire legacy GuildQuest subsystems via adapters
        ServiceBundle services = ServiceBundle.builder()
                .inventoryService(new InventoryAdapter())
                .build();

        Scanner scanner = new Scanner(System.in);
        new ConsoleUI(registry, services, scanner).run();
        scanner.close();
    }
}
