package gmae.adventures.caravanTrade;

import gmae.core.api.InputEvent;
import gmae.core.api.MiniAdventure;
import gmae.core.api.PlayerId;
import gmae.core.model.AdventureState;
import gmae.core.model.Coord;
import gmae.core.model.EntityView;
import gmae.core.model.ItemView;
import gmae.core.model.Outcome;
import gmae.core.model.PlayerView;
import gmae.core.services.GmaeInventoryService;
import gmae.core.services.GmaeRealmService;
import gmae.core.services.ServiceBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A 2-player turn-based trade/delivery mini-adventure.
 *
 * <p>Players move caravans across 5 trade hubs on the shared realm map,
 * pick up local goods, and deliver them to fulfill orders for gold profit.
 * First player to complete {@value #ORDERS_TO_WIN} deliveries wins.</p>
 */
public class CaravanTradeRunAdventure implements MiniAdventure {

    private static final String REALM = "Shadowfen";
    private static final int ORDERS_TO_WIN = 2;
    private static final int INVENTORY_CAPACITY = 4;
    private static final int GRID_SIZE = 10;
    private static final int MAX_COORD = GRID_SIZE - 1;

    private static final String LOC_MARKETPLACE   = "Marketplace";
    private static final String LOC_DOCKS         = "Docks";
    private static final String LOC_CROSSROADS    = "Crossroads";
    private static final String LOC_MOUNTAIN_PASS = "Mountain Pass";
    private static final String LOC_OASIS         = "Oasis";

    private static final Map<String, Coord> LOCATION_COORDS;
    private static final Map<String, List<String>> ADJACENCY;
    private static final Map<String, String> LOCATION_GOODS;

    static {
        Map<String, Coord> coords = new LinkedHashMap<>();
        coords.put(LOC_MARKETPLACE,   new Coord(0, 0));
        coords.put(LOC_DOCKS,         new Coord(9, 0));
        coords.put(LOC_CROSSROADS,    new Coord(5, 5));
        coords.put(LOC_MOUNTAIN_PASS, new Coord(0, 9));
        coords.put(LOC_OASIS,         new Coord(9, 9));
        LOCATION_COORDS = Collections.unmodifiableMap(coords);

        Map<String, List<String>> adj = new LinkedHashMap<>();
        adj.put(LOC_MARKETPLACE,   List.of(LOC_DOCKS, LOC_CROSSROADS));
        adj.put(LOC_DOCKS,         List.of(LOC_MARKETPLACE, LOC_CROSSROADS));
        adj.put(LOC_CROSSROADS,    List.of(LOC_MARKETPLACE, LOC_DOCKS, LOC_MOUNTAIN_PASS, LOC_OASIS));
        adj.put(LOC_MOUNTAIN_PASS, List.of(LOC_CROSSROADS, LOC_OASIS));
        adj.put(LOC_OASIS,         List.of(LOC_CROSSROADS, LOC_MOUNTAIN_PASS));
        ADJACENCY = Collections.unmodifiableMap(adj);

        Map<String, String> goods = new LinkedHashMap<>();
        goods.put(LOC_MARKETPLACE,   "Silk");
        goods.put(LOC_DOCKS,         "Fish");
        goods.put(LOC_CROSSROADS,    "Gems");
        goods.put(LOC_MOUNTAIN_PASS, "Iron");
        goods.put(LOC_OASIS,         "Spice");
        LOCATION_GOODS = Collections.unmodifiableMap(goods);
    }

    private static final class Order {
        final String item;
        final int qty;
        final String destination;
        final int reward;

        Order(String item, int qty, String destination, int reward) {
            this.item = item;
            this.qty = qty;
            this.destination = destination;
            this.reward = reward;
        }

        @Override
        public String toString() {
            return "Deliver " + qty + "x " + item
                    + " to " + destination + " [reward: " + reward + "g]";
        }
    }

    private static final List<Order> P1_ORDERS = List.of(
            new Order("Silk", 2, LOC_OASIS, 50),
            new Order("Iron", 1, LOC_DOCKS, 40),
            new Order("Fish", 2, LOC_MOUNTAIN_PASS, 45)
    );

    private static final List<Order> P2_ORDERS = List.of(
            new Order("Spice", 2, LOC_MARKETPLACE, 55),
            new Order("Gems", 1, LOC_MOUNTAIN_PASS, 35),
            new Order("Fish", 2, LOC_OASIS, 45)
    );

    private GmaeRealmService realm;
    private GmaeInventoryService inventory;

    private int turn;
    private Outcome outcome;
    private final List<String> messages = new ArrayList<>();
    private final Map<PlayerId, InputEvent> inputBuffer = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Integer> orderIndex = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Integer> completedOrders = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Integer> totalProfit = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, Coord> localPlayerPositions = new EnumMap<>(PlayerId.class);

    /** Local fallback when no inventory service is wired. */
    private final Map<PlayerId, Map<String, Integer>> localInventory =
            new EnumMap<>(PlayerId.class);

    @Override public String id()          { return "caravan-trade-run"; }
    @Override public String title()       { return "Caravan Trade Run"; }
    @Override public String description() {
        return "Move goods across the realm - first to complete 2 deliveries wins!";
    }

    @Override
    public void init() {
        turn = 0;
        outcome = Outcome.IN_PROGRESS;
        messages.clear();
        inputBuffer.clear();
        orderIndex.clear();
        completedOrders.clear();
        totalProfit.clear();
        localInventory.clear();
        localPlayerPositions.clear();

        localPlayerPositions.put(PlayerId.P1, LOCATION_COORDS.get(LOC_MARKETPLACE));
        localPlayerPositions.put(PlayerId.P2, LOCATION_COORDS.get(LOC_DOCKS));
        orderIndex.put(PlayerId.P1, 0);
        orderIndex.put(PlayerId.P2, 0);
        completedOrders.put(PlayerId.P1, 0);
        completedOrders.put(PlayerId.P2, 0);
        totalProfit.put(PlayerId.P1, 0);
        totalProfit.put(PlayerId.P2, 0);
        localInventory.put(PlayerId.P1, new LinkedHashMap<>());
        localInventory.put(PlayerId.P2, new LinkedHashMap<>());

        messages.add("Welcome to Caravan Trade Run!");
        messages.add("Trade hubs sit on the realm map - step onto them to buy and deliver goods.");
        messages.add("First to complete " + ORDERS_TO_WIN + " deliveries wins.");
        messages.add("Map hubs: Marketplace (0,0), Docks (9,0), Crossroads (5,5), Mountain Pass (0,9), Oasis (9,9)");
        messages.add("Commands: status, map, look, move north|south|east|west, pickup <item> <qty>, deliver, help");
        messages.add("P1 starts at Marketplace. P2 starts at Docks.");
        messages.add("P1 order: " + currentOrder(PlayerId.P1));
        messages.add("P2 order: " + currentOrder(PlayerId.P2));
    }

    @Override
    public void bindServices(ServiceBundle services) {
        this.realm = services.realmService().orElse(null);
        this.inventory = services.inventoryService().orElse(null);

        if (realm != null) {
            try {
                realm.placePlayer(PlayerId.P1, REALM, LOCATION_COORDS.get(LOC_MARKETPLACE));
                realm.placePlayer(PlayerId.P2, REALM, LOCATION_COORDS.get(LOC_DOCKS));
                placeTradeMarkers();
                messages.add("[Realm service active]");
            } catch (Exception e) {
                messages.add("[Realm service error: " + e.getMessage() + "]");
                realm = null;
            }
        }

        if (inventory != null) {
            messages.add("[Inventory service active]");
        } else {
            messages.add("[Using local inventory fallback]");
        }
    }

    @Override
    public void acceptInput(PlayerId player, InputEvent event) {
        inputBuffer.put(player, event);
    }

    @Override
    public void advance() {
        if (outcome != Outcome.IN_PROGRESS) return;

        turn++;
        messages.clear();
        messages.add("Turn " + turn
                + " | P1 deliveries: " + completedOrders.get(PlayerId.P1) + "/" + ORDERS_TO_WIN
                + " | P2 deliveries: " + completedOrders.get(PlayerId.P2) + "/" + ORDERS_TO_WIN);

        processPlayer(PlayerId.P1);
        processPlayer(PlayerId.P2);
        inputBuffer.clear();

        checkWinCondition();
    }

    @Override
    public AdventureState reportState() {
        Map<PlayerId, PlayerView> players = new EnumMap<>(PlayerId.class);
        players.put(PlayerId.P1, buildPlayerView(PlayerId.P1));
        players.put(PlayerId.P2, buildPlayerView(PlayerId.P2));

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
        if (realm != null) {
            try {
                realm.placePlayer(PlayerId.P1, REALM, LOCATION_COORDS.get(LOC_MARKETPLACE));
                realm.placePlayer(PlayerId.P2, REALM, LOCATION_COORDS.get(LOC_DOCKS));
                placeTradeMarkers();
            } catch (Exception e) {
                messages.add("[Realm error on reset: " + e.getMessage() + "]");
            }
        }
    }

    private void processPlayer(PlayerId player) {
        InputEvent event = inputBuffer.get(player);
        if (event == null) {
            messages.add(player + " passes.");
            return;
        }

        String action = event.action().toUpperCase();
        String payload = event.payload();

        switch (action) {
            case "PASS" -> messages.add(player + " passes.");
            case "HELP" -> handleHelp(player);
            case "STATUS" -> handleStatus(player);
            case "MAP" -> handleMap(player);
            case "LOOK" -> handleLook(player);
            case "MOVE" -> handleMove(player, payload);
            case "PICKUP" -> handlePickup(player, payload);
            case "DELIVER" -> handleDeliver(player);
            default -> messages.add(player + ": Unknown command '" + action.toLowerCase() + "'. Type 'help'.");
        }
    }

    private void handleHelp(PlayerId player) {
        messages.add(player + " commands: status, map, look, move north|south|east|west, pickup <item> <qty>, deliver");
    }

    private void handleStatus(PlayerId player) {
        Coord coord = playerCoord(player).orElse(null);
        String loc = currentLocation(player);
        Order order = currentOrder(player);
        int completed = completedOrders.get(player);
        int profit = totalProfit.get(player);
        List<ItemView> items = listPlayerItems(player);

        messages.add(player + " STATUS:");
        messages.add("  Position : " + formatCoord(coord));
        messages.add("  Location : " + (loc != null ? loc : "On the road"));
        messages.add("  Inventory: " + formatInventory(items)
                + " [" + countInventoryItems(player) + "/" + INVENTORY_CAPACITY + "]");
        messages.add("  Order    : " + (order != null ? order.toString() : "NONE"));
        messages.add("  Completed: " + completed + "/" + ORDERS_TO_WIN);
        messages.add("  Profit   : " + profit + "g");
    }

    private void handleMap(PlayerId player) {
        messages.add(player + " trade hubs:");
        for (Map.Entry<String, Coord> entry : LOCATION_COORDS.entrySet()) {
            String location = entry.getKey();
            Coord coord = entry.getValue();
            messages.add("  " + location + " " + formatCoord(coord)
                    + " sells " + LOCATION_GOODS.get(location)
                    + " | routes: " + String.join(", ", ADJACENCY.get(location)));
        }
    }

    private void handleLook(PlayerId player) {
        Coord coord = playerCoord(player).orElse(null);
        String loc = currentLocation(player);
        if (loc == null) {
            messages.add(player + " is at " + formatCoord(coord) + ", between trade hubs.");
            return;
        }

        messages.add(player + " looks around " + loc + " " + formatCoord(coord) + ":");
        messages.add("  Goods here : " + LOCATION_GOODS.get(loc));
        messages.add("  Routes to  : " + String.join(", ", ADJACENCY.get(loc)));
    }

    private void handleMove(PlayerId player, String payload) {
        Direction direction = Direction.parse(payload);
        if (direction == null) {
            messages.add(player + ": Use 'move north', 'move south', 'move east', or 'move west'.");
            return;
        }

        if (!movePlayer(player, direction.dx, direction.dy)) {
            messages.add(player + ": Cannot move " + direction.label
                    + " from " + formatCoord(playerCoord(player).orElse(null)) + ".");
            return;
        }

        Coord coord = playerCoord(player).orElse(null);
        String loc = currentLocation(player);
        if (loc != null) {
            messages.add(player + " moves " + direction.label + " to " + loc + " " + formatCoord(coord) + ".");
        } else {
            messages.add(player + " moves " + direction.label + " to " + formatCoord(coord)
                    + ", between trade hubs.");
        }
    }

    private void handlePickup(PlayerId player, String payload) {
        if (payload == null || payload.isBlank()) {
            messages.add(player + ": Usage: pickup <item> <qty>");
            return;
        }

        String[] parts = payload.trim().split("\\s+");
        String itemName;
        int qty;

        if (parts.length >= 2) {
            try {
                qty = Integer.parseInt(parts[parts.length - 1]);
                StringBuilder sb = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length - 1; i++) {
                    sb.append(' ').append(parts[i]);
                }
                itemName = sb.toString();
            } catch (NumberFormatException e) {
                itemName = payload.trim();
                qty = 1;
            }
        } else {
            itemName = parts[0];
            qty = 1;
        }

        if (qty <= 0) {
            messages.add(player + ": Quantity must be positive.");
            return;
        }

        String loc = currentLocation(player);
        if (loc == null) {
            messages.add(player + ": You must be standing on a trade location to pick up goods.");
            return;
        }

        String availableGoods = LOCATION_GOODS.get(loc);
        if (!availableGoods.equalsIgnoreCase(itemName)) {
            messages.add(player + ": " + itemName + " is not available at " + loc
                    + ". This location sells " + availableGoods + ".");
            return;
        }

        int currentTotal = countInventoryItems(player);
        if (currentTotal + qty > INVENTORY_CAPACITY) {
            int canFit = INVENTORY_CAPACITY - currentTotal;
            messages.add(player + ": Not enough capacity! ["
                    + currentTotal + "/" + INVENTORY_CAPACITY
                    + "] Can pick up at most " + canFit + " more.");
            return;
        }

        addToInventory(player, availableGoods, qty);
        messages.add(player + " picks up " + qty + "x " + availableGoods
                + " at " + loc + ". ["
                + (currentTotal + qty) + "/" + INVENTORY_CAPACITY + " items]");
    }

    private void handleDeliver(PlayerId player) {
        Order order = currentOrder(player);
        if (order == null) {
            messages.add(player + ": No active order.");
            return;
        }

        String loc = currentLocation(player);
        if (loc == null) {
            messages.add(player + ": You must be standing on " + order.destination
                    + " to deliver. Current position: " + formatCoord(playerCoord(player).orElse(null)) + ".");
            return;
        }
        if (!loc.equals(order.destination)) {
            messages.add(player + ": Must be at " + order.destination
                    + " to deliver. Currently at " + loc + ".");
            return;
        }

        if (!hasItems(player, order.item, order.qty)) {
            messages.add(player + ": Need " + order.qty + "x " + order.item
                    + " to deliver. Check 'status' for inventory.");
            return;
        }

        removeFromInventory(player, order.item, order.qty);

        int completed = completedOrders.get(player) + 1;
        int profit = totalProfit.get(player) + order.reward;
        completedOrders.put(player, completed);
        totalProfit.put(player, profit);

        messages.add(player + " delivers " + order.qty + "x " + order.item
                + " to " + order.destination + " for +" + order.reward + "g.");
        messages.add("  Total profit: " + profit + "g | Deliveries: " + completed + "/" + ORDERS_TO_WIN);

        int nextIdx = orderIndex.get(player) + 1;
        orderIndex.put(player, nextIdx);
        Order next = currentOrder(player);
        if (next != null && completed < ORDERS_TO_WIN) {
            messages.add("  Next order: " + next);
        }
    }

    private void checkWinCondition() {
        int p1 = completedOrders.get(PlayerId.P1);
        int p2 = completedOrders.get(PlayerId.P2);

        if (p1 >= ORDERS_TO_WIN && p2 >= ORDERS_TO_WIN) {
            outcome = Outcome.DRAW;
            messages.add("DRAW! Both traders completed " + ORDERS_TO_WIN + " deliveries.");
            messages.add("P1: " + totalProfit.get(PlayerId.P1) + "g | P2: " + totalProfit.get(PlayerId.P2) + "g");
        } else if (p1 >= ORDERS_TO_WIN) {
            outcome = Outcome.P1_WINS;
            messages.add("P1 wins with " + totalProfit.get(PlayerId.P1) + "g profit.");
        } else if (p2 >= ORDERS_TO_WIN) {
            outcome = Outcome.P2_WINS;
            messages.add("P2 wins with " + totalProfit.get(PlayerId.P2) + "g profit.");
        }
    }

    private Order currentOrder(PlayerId player) {
        List<Order> pool = player == PlayerId.P1 ? P1_ORDERS : P2_ORDERS;
        int idx = orderIndex.getOrDefault(player, 0);
        return idx < pool.size() ? pool.get(idx) : null;
    }

    private PlayerView buildPlayerView(PlayerId player) {
        Map<String, String> attrs = new LinkedHashMap<>();
        Coord coord = playerCoord(player).orElse(null);
        String loc = currentLocation(player);
        Order order = currentOrder(player);

        attrs.put("position", formatCoord(coord));
        attrs.put("location", loc != null ? loc : "On the road");
        attrs.put("order", order != null ? order.toString() : "NONE");
        attrs.put("deliveries", completedOrders.getOrDefault(player, 0) + "/" + ORDERS_TO_WIN);
        attrs.put("profit", totalProfit.getOrDefault(player, 0) + "g");
        attrs.put("inventory", formatInventory(listPlayerItems(player)));

        return new PlayerView(player, player.name(), totalProfit.getOrDefault(player, 0), attrs);
    }

    private List<ItemView> listPlayerItems(PlayerId player) {
        if (inventory != null) return inventory.listItems(player);

        List<ItemView> result = new ArrayList<>();
        Map<String, Integer> local = localInventory.get(player);
        if (local != null) {
            for (Map.Entry<String, Integer> entry : local.entrySet()) {
                result.add(new ItemView(
                        player + ":" + entry.getKey().toLowerCase(),
                        entry.getKey(),
                        "Trade goods",
                        entry.getValue()
                ));
            }
        }
        return result;
    }

    private int countInventoryItems(PlayerId player) {
        int total = 0;
        for (ItemView item : listPlayerItems(player)) {
            total += item.quantity();
        }
        return total;
    }

    private boolean hasItems(PlayerId player, String itemName, int qty) {
        for (ItemView item : listPlayerItems(player)) {
            if (item.name().equalsIgnoreCase(itemName) && item.quantity() >= qty) {
                return true;
            }
        }
        return false;
    }

    private void addToInventory(PlayerId player, String name, int qty) {
        if (inventory != null) {
            inventory.addItem(player, name, "Trade goods", qty);
        } else {
            localInventory.get(player).merge(name, qty, Integer::sum);
        }
    }

    private void removeFromInventory(PlayerId player, String name, int qty) {
        if (inventory != null) {
            inventory.removeItem(player, name, qty);
        } else {
            Map<String, Integer> local = localInventory.get(player);
            int remaining = local.getOrDefault(name, 0) - qty;
            if (remaining <= 0) {
                local.remove(name);
            } else {
                local.put(name, remaining);
            }
        }
    }

    private String formatInventory(List<ItemView> items) {
        if (items.isEmpty()) return "(empty)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).quantity()).append("x ").append(items.get(i).name());
        }
        return sb.toString();
    }

    private Optional<Coord> playerCoord(PlayerId player) {
        if (realm != null) {
            return realm.playerPosition(player);
        }
        return Optional.ofNullable(localPlayerPositions.get(player));
    }

    private String currentLocation(PlayerId player) {
        return playerCoord(player).map(this::locationAt).orElse(null);
    }

    private String locationAt(Coord coord) {
        for (Map.Entry<String, Coord> entry : LOCATION_COORDS.entrySet()) {
            if (entry.getValue().equals(coord)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean movePlayer(PlayerId player, int dx, int dy) {
        if (realm != null) {
            return realm.movePlayer(player, dx, dy);
        }

        Coord current = localPlayerPositions.get(player);
        if (current == null) {
            return false;
        }

        Coord next = new Coord(current.x() + dx, current.y() + dy);
        if (!isInBounds(next)) {
            return false;
        }

        localPlayerPositions.put(player, next);
        return true;
    }

    private boolean isInBounds(Coord coord) {
        return coord != null
                && coord.x() >= 0 && coord.x() <= MAX_COORD
                && coord.y() >= 0 && coord.y() <= MAX_COORD;
    }

    private String formatCoord(Coord coord) {
        return coord == null ? "?" : "(" + coord.x() + "," + coord.y() + ")";
    }

    private void placeTradeMarkers() {
        if (realm == null) {
            return;
        }

        for (Map.Entry<String, Coord> entry : LOCATION_COORDS.entrySet()) {
            realm.addEntity(new EntityView(
                    "trade-" + entry.getKey().toLowerCase().replace(' ', '-'),
                    entry.getKey(),
                    "trade-location",
                    entry.getValue(),
                    REALM
            ));
        }
    }

    private enum Direction {
        NORTH("NORTH", 0, -1),
        SOUTH("SOUTH", 0, 1),
        EAST("EAST", 1, 0),
        WEST("WEST", -1, 0);

        private final String label;
        private final int dx;
        private final int dy;

        Direction(String label, int dx, int dy) {
            this.label = label;
            this.dx = dx;
            this.dy = dy;
        }

        private static Direction parse(String payload) {
            if (payload == null) {
                return null;
            }
            return switch (payload.trim().toUpperCase()) {
                case "NORTH" -> NORTH;
                case "SOUTH" -> SOUTH;
                case "EAST" -> EAST;
                case "WEST" -> WEST;
                default -> null;
            };
        }
    }
}
