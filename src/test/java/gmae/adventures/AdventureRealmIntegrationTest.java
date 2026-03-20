package gmae.adventures;

import gmae.adventures.caravanTrade.CaravanTradeRunAdventure;
import gmae.adventures.relicHunt.RelicHuntAdventure;
import gmae.core.api.InputEvent;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Coord;
import gmae.core.services.ServiceBundle;
import gmae.core.services.fakes.FakeInventoryService;
import gmae.core.services.fakes.FakeRealmService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdventureRealmIntegrationTest {

    @Test
    void relicHuntCollectsRelicsFromRealmEntityPositions() {
        FakeRealmService realm = new FakeRealmService();
        RelicHuntAdventure adventure = new RelicHuntAdventure();

        adventure.init();
        adventure.bindServices(ServiceBundle.builder().realmService(realm).build());

        realm.placePlayer(PlayerId.P1, "Shadowfen", new Coord(1, 2));
        adventure.acceptInput(PlayerId.P1, InputEvent.of("MOVE", "EAST"));
        adventure.acceptInput(PlayerId.P2, InputEvent.of("PASS"));
        adventure.advance();

        AdventureState state = adventure.reportState();
        assertEquals("1", state.players().get(PlayerId.P1).attributes().get("relics"));
        assertTrue(realm.listEntitiesInRealm("Shadowfen").stream()
                .noneMatch(entity -> "relic-1".equals(entity.id())));
    }

    @Test
    void caravanTradeUsesPlayerCoordinatesForTradeLocations() {
        FakeRealmService realm = new FakeRealmService();
        FakeInventoryService inventory = new FakeInventoryService();
        CaravanTradeRunAdventure adventure = new CaravanTradeRunAdventure();

        adventure.init();
        adventure.bindServices(ServiceBundle.builder()
                .realmService(realm)
                .inventoryService(inventory)
                .build());

        realm.placePlayer(PlayerId.P1, "Shadowfen", new Coord(5, 5));
        adventure.acceptInput(PlayerId.P1, InputEvent.of("PICKUP", "Gems 1"));
        adventure.acceptInput(PlayerId.P2, InputEvent.of("PASS"));
        adventure.advance();

        AdventureState state = adventure.reportState();
        assertEquals("(5,5)", state.players().get(PlayerId.P1).attributes().get("position"));
        assertEquals("Crossroads", state.players().get(PlayerId.P1).attributes().get("location"));
        assertEquals("1x Gems", state.players().get(PlayerId.P1).attributes().get("inventory"));

        adventure.acceptInput(PlayerId.P1, InputEvent.of("MOVE", "EAST"));
        adventure.acceptInput(PlayerId.P2, InputEvent.of("PASS"));
        adventure.advance();
        adventure.acceptInput(PlayerId.P1, InputEvent.of("PICKUP", "Gems 1"));
        adventure.acceptInput(PlayerId.P2, InputEvent.of("PASS"));
        adventure.advance();

        state = adventure.reportState();
        assertEquals("(6,5)", state.players().get(PlayerId.P1).attributes().get("position"));
        assertEquals("On the road", state.players().get(PlayerId.P1).attributes().get("location"));
        assertTrue(state.messages().stream()
                .anyMatch(message -> message.contains("standing on a trade location")));
    }
}
