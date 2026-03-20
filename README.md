# GMAE — GuildQuest Mini-Adventure Environment

A modular, extensible mini-adventure framework built for the INF 122 final project.
Two local players share one machine and choose from a menu of GuildQuest-themed adventures
playable in **console** or **2D GUI** mode.

---

## Features

| Requirement | Status |
|---|---|
| 2+ mini-adventures via `MiniAdventure` interface | **Relic Hunt** (competitive, real-time) + **Caravan Trade Run** (competitive, turn-based) |
| Two local players on one machine | Both adventures support P1 and P2 |
| Menu of adventures | Console menu + GUI selection dialog |
| Player profiles | Names entered at launch; per-player stats tracked in-game |
| Reuse of 2+ prior GuildQuest subsystems | **Realm/map** (`RealmAdapter`) + **Item/inventory** (`InventoryAdapter` wrapping legacy `guildquest.inventory`) |
| Defined interface | Frozen `MiniAdventure` (10 lifecycle methods) |
| 2D GUI (extra credit) | Java Swing + Java2D for both adventures |
| Real-time play (extra credit) | Relic Hunt GUI runs at 150 ms tick with simultaneous WASD / Arrow key input |

---

## Prerequisites

- **Java 17+**
- **Apache Maven 3.8+**

---

## How to Run

### Console mode

```bash
mvn compile
mvn -q exec:java
```

The console displays a numbered menu of registered adventures.
Select one, enter player names, and play turn-by-turn in the terminal.

### GUI mode

```bash
mvn compile
mvn -q exec:java -Dexec.args="--gui"
```

A dialog asks for player names, then presents an adventure picker:

| Adventure | GUI style |
|---|---|
| **Relic Hunt** | Real-time 2D top-down arena — P1 uses **WASD**, P2 uses **Arrow keys** |
| **Caravan Trade Run** | Turn-based panel — type commands or click action buttons |

### Running tests

```bash
mvn test
```

Runs JUnit 5 contract tests for service interfaces and adapter implementations.

---

## Mini-Adventures

### Relic Hunt (`relic-hunt`)

Two players race across a 10×10 grid to collect hidden relics.
First to collect 2 relics wins. Supports both console (turn-based) and GUI (real-time) modes.

- Uses **realm/map** service for grid layout, player positioning, and entity tracking.

### Caravan Trade Run (`caravan-trade-run`)

Two players compete as merchants on a 5-location trade network.
Pick up goods, travel to destinations, and deliver trade orders for profit.
First to complete 2 deliveries wins.

- Uses **realm/map** service for location adjacency and travel.
- Uses **item/inventory** service for pickup, storage, and removal of trade goods.

### Demo (`demo`)

Minimal skeleton adventure used for early engine validation. Not intended for grading.

---

## Reused GuildQuest Subsystems

### 1. Realm / Map

| What | Where |
|---|---|
| Service interface | `gmae/core/services/GmaeRealmService.java` |
| Adapter | `gmae/adapters/RealmAdapter.java` |
| DTOs | `gmae/core/model/Coord.java`, `EntityView.java`, `RealmView.java` |
| Used by | `RelicHuntAdventure`, `CaravanTradeRunAdventure` |

`RealmAdapter` is an in-memory reference implementation managing realms, 10×10 coordinate grids, player positions, and entity placement.

### 2. Item / Inventory

| What | Where |
|---|---|
| Service interface | `gmae/core/services/GmaeInventoryService.java` |
| Adapter | `gmae/adapters/InventoryAdapter.java` |
| Legacy code wrapped | `guildquest/inventory/Inventory.java`, `guildquest/inventory/Items.java` |
| DTO | `gmae/core/model/ItemView.java` |
| Used by | `CaravanTradeRunAdventure` |

`InventoryAdapter` wraps the legacy `Inventory`/`Items` classes (ported from a prior Python assignment) behind the GMAE service contract, bridging the mutable single-item model to a per-player, quantity-stacked interface.

---

## Architecture

```
gmae/
  Main.java                        # Entry point — registers adventures, selects console/GUI
  core/
    api/                           # Frozen public API
      MiniAdventure.java           #   adventure lifecycle interface (10 methods)
      InputEvent.java              #   player action DTO
      PlayerId.java                #   P1 / P2 enum
    model/                         # Frozen engine DTOs
      AdventureState.java          #   full game-state snapshot (Builder pattern)
      PlayerView.java              #   per-player score + attributes
      Outcome.java                 #   win/loss/draw enum
      Coord.java                   #   2D coordinate
      EntityView.java              #   realm entity snapshot
      RealmView.java               #   realm identity snapshot
      ItemView.java                #   inventory item snapshot
      TimeView.java                #   world-time snapshot
      QuestEventView.java          #   quest event snapshot
    services/                      # Frozen service interfaces
      ServiceBundle.java           #   dependency-injection container (Builder pattern)
      GmaeRealmService.java        #   realm/map operations
      GmaeInventoryService.java    #   inventory operations
      GmaeTimeService.java         #   time operations
      GmaeQuestEventService.java   #   quest/event operations
    engine/                        # Engine internals (hidden from adventures)
      AdventureRegistry.java       #   discovery + factory registry
      AdventureManager.java        #   lifecycle orchestrator
      GameLoop.java                #   console turn driver
      InputRouter.java             #   raw-text → InputEvent parser
  adventures/
    relicHunt/
      RelicHuntAdventure.java      # Competitive relic-collection adventure
    caravanTrade/
      CaravanTradeRunAdventure.java # Competitive trade/delivery adventure
    demo/
      DemoAdventure.java           # Minimal engine-validation skeleton
  adapters/
    RealmAdapter.java              # GmaeRealmService implementation
    InventoryAdapter.java          # GmaeInventoryService → legacy Inventory wrapper
  ui/
    ConsoleUI.java                 # Console menu + launch flow
    gui/
      GuiLauncher.java             # GUI entry point + adventure selection
      GamePanel.java               # Relic Hunt real-time renderer (Swing)
      GuiSession.java              # Relic Hunt real-time controller (Timer loop)
      KeyInputHandler.java         # 2-player keyboard input (WASD + Arrows)
      CaravanPanel.java            # Caravan Trade Run GUI panel
      CaravanSession.java          # Caravan Trade Run turn-based controller

guildquest/                        # Legacy code — NOT modified by this project
  inventory/
    Inventory.java                 # Legacy inventory model (wrapped by InventoryAdapter)
    Items.java                     # Legacy item model
```

---

## Architecture Rules

1. **Adventures MUST NOT import** `guildquest.**`, `gmae.adapters.**`, or `gmae.core.engine.**`.
2. Adventures may **only** depend on:
   - `gmae.core.api.*` — `MiniAdventure`, `InputEvent`, `PlayerId`
   - `gmae.core.model.*` — all DTOs
   - `gmae.core.services.*` — service interfaces + `ServiceBundle`
3. All legacy GuildQuest integration goes through **adapters**.
4. The `MiniAdventure` interface and service contracts are **frozen** — only backward-compatible additions allowed.

---

## Design Patterns

| Pattern | Where | Purpose |
|---|---|---|
| **Adapter** | `RealmAdapter`, `InventoryAdapter` | Wrap legacy/reference subsystems behind stable GMAE service interfaces |
| **Builder** | `AdventureState.Builder`, `ServiceBundle.Builder` | Construct complex immutable objects step-by-step |
| **Single Access Point** | `AdventureManager` | Single facade through which all UI layers interact with adventures |
| **Information Hiding / Facade** | `gmae.core.engine` package | Engine internals hidden from adventures; only `core.api`, `core.model`, `core.services` are public |

---

## Testing

```
src/test/java/
  gmae/
    core/services/          # Contract tests against service interfaces
      RealmContractTest.java
      InventoryContractTest.java
      TimeContractTest.java
      QuestEventContractTest.java
      fakes/                # Reference fake implementations for testing
        FakeRealmService.java
        FakeInventoryService.java
        FakeTimeService.java
        FakeQuestEventService.java
    adapters/               # Adapter-specific contract tests
      RealmAdapterContractTest.java
      InventoryAdapterContractTest.java
      TimeAdapterContractTest.java
      QuestEventAdapterContractTest.java
```

---

## Adding a New Mini-Adventure

1. Create a class implementing `MiniAdventure` in a new sub-package under `gmae/adventures/`.
2. Implement the 9 required methods (`id`, `title`, `description`, `init`, `acceptInput`, `advance`, `reportState`, `isComplete`, `reset`). `bindServices()` has a default no-op.
3. Use services only through `ServiceBundle` — never import adapters or legacy code.
4. Register in `Main.java`: `registry.register(YourAdventure::new);`
5. (Optional) Add a dedicated GUI panel and session class in `gmae/ui/gui/` and route from `GuiLauncher`.

See `docs/mini_adventure_contract.md` for the full lifecycle specification.

---

## Documentation

| Document | Description |
|---|---|
| `docs/mini_adventure_contract.md` | Frozen `MiniAdventure` lifecycle contract |
| `docs/services_contract.md` | Service interface specifications |
| `docs/adapter_test_harness.md` | Test harness and contract testing guide |
| `docs/architecture_inventory.md` | Architecture inventory and module map |
| `docs/OWNERSHIP.md` | File/folder ownership between sub-teams |
| `docs/CONTRIBUTING.md` | Contribution and merge guidelines |
