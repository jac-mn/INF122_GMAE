# GMAE Architecture Inventory

> Auto-generated from source scan. Covers all 21 Java files under
> `src/main/java/gmae/`. No UML — structured text only.

---

## 1. Package Map

```
gmae/                           1 class   (Main)
gmae.core.api/                  1 interface, 1 class, 1 enum
gmae.core.model/                6 classes, 1 enum
gmae.core.services/             3 interfaces, 1 class
gmae.core.engine/               4 classes
gmae.ui/                        1 class
gmae.adventures.demo/           1 class
                         ──────────────────
                         21 files total
```

---

## 2. Per-Package Inventory

### 2.1 `gmae` — Entry Point

#### `Main`
- **File:** `src/main/java/gmae/Main.java`
- **Type:** `public class`
- **Fields:** (none)
- **Methods:**
  - `static void main(String[] args)`
- **Relationships:**
  - *creates* `AdventureRegistry`
  - *calls* `registry.register(DemoAdventure::new)`
  - *creates* `ConsoleUI(registry, scanner)`
  - *calls* `ConsoleUI.run()`

---

### 2.2 `gmae.core.api` — Public Contract (frozen)

#### `PlayerId`
- **File:** `src/main/java/gmae/core/api/PlayerId.java`
- **Type:** `public enum`
- **Constants:** `P1`, `P2`
- **Dependencies:** none

#### `InputEvent`
- **File:** `src/main/java/gmae/core/api/InputEvent.java`
- **Type:** `public final class`
- **Fields:** `String action` (non-null), `String payload` (nullable)
- **Methods:**
  - `InputEvent(String action, String payload)`
  - `static InputEvent of(String action)`
  - `static InputEvent of(String action, String payload)`
  - `String action()`, `String payload()`
  - `equals()`, `hashCode()`, `toString()`
- **Dependencies:** none

#### `MiniAdventure`
- **File:** `src/main/java/gmae/core/api/MiniAdventure.java`
- **Type:** `public interface`
- **Methods:**
  - `String id()` — stable registry key
  - `String title()` — display name
  - `String description()` — menu text
  - `void init()` — one-time setup
  - `default void bindServices(ServiceBundle)` — service injection (no-op default)
  - `void acceptInput(PlayerId, InputEvent)` — buffer input
  - `void advance()` — process one step
  - `AdventureState reportState()` — snapshot
  - `boolean isComplete()` — terminal check
  - `void reset()` — replay
- **Dependencies:**
  - *uses* `PlayerId`, `InputEvent`, `ServiceBundle`
  - *returns* `AdventureState`

---

### 2.3 `gmae.core.model` — DTOs (frozen)

#### `Outcome`
- **File:** `src/main/java/gmae/core/model/Outcome.java`
- **Type:** `public enum`
- **Constants:** `IN_PROGRESS`, `P1_WINS`, `P2_WINS`, `DRAW`, `COOP_WIN`, `LOSS`
- **Dependencies:** none

#### `PlayerView`
- **File:** `src/main/java/gmae/core/model/PlayerView.java`
- **Type:** `public final class`
- **Fields:** `PlayerId id`, `String name`, `int score`, `Map<String,String> attributes`
- **Methods:**
  - `PlayerView(PlayerId, String, int, Map<String,String>)`
  - `PlayerView(PlayerId, String, int)` — convenience (empty attributes)
  - `id()`, `name()`, `score()`, `attributes()`
  - `equals()`, `hashCode()`, `toString()`
- **Dependencies:** *uses* `PlayerId`

#### `AdventureState`
- **File:** `src/main/java/gmae/core/model/AdventureState.java`
- **Type:** `public final class` (with inner `Builder`)
- **Fields:** `String title`, `int tickOrTurn`, `List<String> messages`, `Map<PlayerId, PlayerView> players`, `boolean complete`, `Outcome outcome`
- **Methods:**
  - `title()`, `tickOrTurn()`, `messages()`, `players()`, `complete()`, `outcome()`
  - `static Builder builder()`
  - `equals()`, `hashCode()`, `toString()`
- **Builder methods:** `title()`, `tickOrTurn()`, `messages()`, `players()`, `complete()`, `outcome()`, `build()`
- **Dependencies:** *uses* `PlayerId`, `PlayerView`, `Outcome`

#### `ItemView`
- **File:** `src/main/java/gmae/core/model/ItemView.java`
- **Type:** `public final class`
- **Fields:** `String id`, `String name`, `String description`, `int quantity`
- **Methods:** constructor, `id()`, `name()`, `description()`, `quantity()`, `equals()`, `hashCode()`, `toString()`
- **Dependencies:** none

#### `TimeView`
- **File:** `src/main/java/gmae/core/model/TimeView.java`
- **Type:** `public final class`
- **Fields:** `int days`, `int hours`, `int minutes`, `String localTimeString` (nullable)
- **Methods:**
  - `TimeView(int, int, int, String)`, `TimeView(int, int, int)`
  - `days()`, `hours()`, `minutes()`, `localTimeString()`, `toTotalMinutes()`
  - `equals()`, `hashCode()`, `toString()`
- **Dependencies:** none

#### `RealmView`
- **File:** `src/main/java/gmae/core.model/RealmView.java`
- **Type:** `public final class`
- **Fields:** `String id`, `String name`, `String description`
- **Methods:** constructor, `id()`, `name()`, `description()`, `equals()`, `hashCode()`, `toString()`
- **Dependencies:** none

#### `QuestEventView`
- **File:** `src/main/java/gmae/core/model/QuestEventView.java`
- **Type:** `public final class`
- **Fields:** `String id`, `String title`, `long startMinutes`, `long endMinutes`, `String realmName`
- **Methods:** constructor, `id()`, `title()`, `startMinutes()`, `endMinutes()`, `realmName()`, `isOpenEnded()`, `equals()`, `hashCode()`, `toString()`
- **Dependencies:** none

---

### 2.4 `gmae.core.services` — Service Interfaces (frozen)

#### `GmaeTimeService`
- **File:** `src/main/java/gmae/core/services/GmaeTimeService.java`
- **Type:** `public interface`
- **Methods:**
  - `TimeView nowWorld()`
  - `Optional<TimeView> toLocal(String realmName)`
  - `void advanceMinutes(int minutes)`
- **Dependencies:** *returns* `TimeView`

#### `GmaeInventoryService`
- **File:** `src/main/java/gmae/core/services/GmaeInventoryService.java`
- **Type:** `public interface`
- **Methods:**
  - `List<ItemView> listItems(PlayerId player)`
  - `void addItem(PlayerId player, String name, String description, int qty)`
  - `boolean removeItem(PlayerId player, String itemName, int qty)`
- **Dependencies:** *uses* `PlayerId`; *returns* `ItemView`

#### `GmaeQuestEventService`
- **File:** `src/main/java/gmae/core/services/GmaeQuestEventService.java`
- **Type:** `public interface`
- **Methods:**
  - `List<QuestEventView> listEvents()`
  - `String addEvent(QuestEventView event)`
  - `boolean removeEvent(String eventId)`
- **Dependencies:** *uses/returns* `QuestEventView`

#### `ServiceBundle`
- **File:** `src/main/java/gmae/core/services/ServiceBundle.java`
- **Type:** `public final class` (with inner `Builder`)
- **Fields:** `GmaeTimeService timeService`, `GmaeInventoryService inventoryService`, `GmaeQuestEventService questEventService`
- **Methods:**
  - `Optional<GmaeTimeService> timeService()`
  - `Optional<GmaeInventoryService> inventoryService()`
  - `Optional<GmaeQuestEventService> questEventService()`
  - `static ServiceBundle empty()`
  - `static Builder builder()`
- **Builder methods:** `timeService()`, `inventoryService()`, `questEventService()`, `build()`
- **Dependencies:** *holds* `GmaeTimeService`, `GmaeInventoryService`, `GmaeQuestEventService`

---

### 2.5 `gmae.core.engine` — Engine Internals (not importable by adventures)

#### `AdventureRegistry`
- **File:** `src/main/java/gmae/core/engine/AdventureRegistry.java`
- **Type:** `public class`
- **Inner type:** `public record EntryInfo(String id, String title, String description)`
- **Fields:** `Map<String, EntryInfo> metadata`, `Map<String, Supplier<MiniAdventure>> factories`
- **Methods:**
  - `void register(Supplier<MiniAdventure> factory)` — probes factory once for metadata
  - `List<EntryInfo> listAdventures()`
  - `MiniAdventure create(String id)` — calls `factory.get()`
  - `boolean has(String id)`
- **Relationships:**
  - *creates* `MiniAdventure` instances (via supplier)
  - *returns* `EntryInfo` list to callers

#### `AdventureManager`
- **File:** `src/main/java/gmae/core/engine/AdventureManager.java`
- **Type:** `public class`
- **Fields:** `AdventureRegistry registry`, `ServiceBundle services`, `MiniAdventure current`
- **Methods:**
  - `AdventureManager(AdventureRegistry, ServiceBundle)`
  - `AdventureManager(AdventureRegistry)` — uses `ServiceBundle.empty()`
  - `AdventureState start(String adventureId)` — creates, inits, binds services
  - `void submitInput(PlayerId, InputEvent)` — delegates to `current.acceptInput()`
  - `AdventureState step()` — calls `current.advance()` + `current.reportState()`
  - `boolean isFinished()` — delegates to `current.isComplete()`
  - `AdventureState restart()` — calls `current.reset()` + `current.reportState()`
  - `AdventureState currentState()` — calls `current.reportState()`
- **Relationships:**
  - *uses* `AdventureRegistry` to create adventures
  - *calls* `MiniAdventure.init()`, `.bindServices()`, `.acceptInput()`, `.advance()`, `.reportState()`, `.isComplete()`, `.reset()`
  - *holds* `ServiceBundle`, passes it via `bindServices()`
  - *returns* `AdventureState`

#### `InputRouter`
- **File:** `src/main/java/gmae/core/engine/InputRouter.java`
- **Type:** `public class`
- **Fields:** (none — stateless)
- **Methods:**
  - `InputEvent parse(String raw)` — WASD/IJKL shortcuts + text commands
- **Relationships:**
  - *returns* `InputEvent`

#### `GameLoop`
- **File:** `src/main/java/gmae/core/engine/GameLoop.java`
- **Type:** `public class`
- **Fields:** `AdventureManager manager`, `InputRouter inputRouter`, `Scanner scanner`
- **Methods:**
  - `GameLoop(AdventureManager, InputRouter, Scanner)`
  - `void run(String adventureId, String p1Name, String p2Name)` — turn-based loop with replay
- **Relationships:**
  - *calls* `AdventureManager.start()`, `.submitInput()`, `.step()`, `.isFinished()`, `.restart()`
  - *calls* `InputRouter.parse()`
  - *reads* `AdventureState`, `PlayerView`, `Outcome` for display

---

### 2.6 `gmae.ui` — Console UI

#### `ConsoleUI`
- **File:** `src/main/java/gmae/ui/ConsoleUI.java`
- **Type:** `public class`
- **Fields:** `AdventureRegistry registry`, `Scanner scanner`
- **Methods:**
  - `ConsoleUI(AdventureRegistry, Scanner)`
  - `void run()` — main menu loop
  - `private void playAdventure()` — lists adventures, collects player names, runs game
- **Relationships:**
  - *reads* `AdventureRegistry.listAdventures()` → `EntryInfo`
  - *creates* `AdventureManager(registry)`
  - *creates* `InputRouter`
  - *creates* `GameLoop(manager, router, scanner)`
  - *calls* `GameLoop.run()`

---

### 2.7 `gmae.adventures.demo` — Demo Plugin

#### `DemoAdventure`
- **File:** `src/main/java/gmae/adventures/demo/DemoAdventure.java`
- **Type:** `public class`
- **Implements:** `MiniAdventure`
- **Fields:** `int MAX_TURNS` (static, 3), `int turn`, `int p1Score`, `int p2Score`, `Outcome outcome`, `List<String> messages`, `Map<PlayerId, InputEvent> inputBuffer`
- **Methods:** `id()`, `title()`, `description()`, `init()`, `acceptInput()`, `advance()`, `reportState()`, `isComplete()`, `reset()`
- **Imports from gmae.***:
  - `gmae.core.api.InputEvent`, `gmae.core.api.MiniAdventure`, `gmae.core.api.PlayerId`
  - `gmae.core.model.AdventureState`, `gmae.core.model.Outcome`, `gmae.core.model.PlayerView`
  - (no `gmae.core.engine`, no `gmae.core.services`, no `gmae.adapters`, no `guildquest`)
- **Does NOT override:** `bindServices()` — uses the default no-op

---

## 3. Relationship Summary

### 3.1 "implements" relationships

| Implementation | Interface |
|----------------|-----------|
| `DemoAdventure` | `MiniAdventure` |
| *(future adapters)* | `GmaeTimeService` |
| *(future adapters)* | `GmaeInventoryService` |
| *(future adapters)* | `GmaeQuestEventService` |

### 3.2 "creates" relationships

| Creator | Created Type | How |
|---------|-------------|-----|
| `Main` | `AdventureRegistry` | `new AdventureRegistry()` |
| `Main` | `ConsoleUI` | `new ConsoleUI(registry, scanner)` |
| `AdventureRegistry.register()` | `MiniAdventure` (probe) | `factory.get()` — discarded after reading metadata |
| `AdventureRegistry.create()` | `MiniAdventure` (fresh) | `factory.get()` — returned to caller |
| `AdventureManager.start()` | `MiniAdventure` | via `registry.create(adventureId)` |
| `ConsoleUI.playAdventure()` | `AdventureManager` | `new AdventureManager(registry)` |
| `ConsoleUI.playAdventure()` | `InputRouter` | `new InputRouter()` |
| `ConsoleUI.playAdventure()` | `GameLoop` | `new GameLoop(manager, router, scanner)` |
| `DemoAdventure.reportState()` | `AdventureState` | via `AdventureState.builder()…build()` |
| `DemoAdventure.reportState()` | `PlayerView` (×2) | `new PlayerView(…)` |

### 3.3 "calls" chain (runtime flow)

```
Main.main()
 ├─ registry.register(DemoAdventure::new)
 └─ ConsoleUI.run()
     └─ playAdventure()
         ├─ registry.listAdventures()        → List<EntryInfo>
         ├─ new AdventureManager(registry)
         ├─ new GameLoop(manager, router, scanner)
         └─ GameLoop.run(adventureId, p1Name, p2Name)
             ├─ manager.start(adventureId)
             │   ├─ registry.create(id)       → MiniAdventure
             │   ├─ adventure.init()
             │   ├─ adventure.bindServices(services)   ◄── SERVICE INJECTION
             │   └─ adventure.reportState()   → AdventureState
             │
             ├─ [loop while !manager.isFinished()]
             │   ├─ inputRouter.parse(raw)    → InputEvent
             │   ├─ manager.submitInput(P1, event)
             │   │   └─ adventure.acceptInput(P1, event)
             │   ├─ inputRouter.parse(raw)    → InputEvent
             │   ├─ manager.submitInput(P2, event)
             │   │   └─ adventure.acceptInput(P2, event)
             │   └─ manager.step()
             │       ├─ adventure.advance()
             │       └─ adventure.reportState()  → AdventureState
             │
             └─ [replay?]
                 └─ manager.restart()
                     ├─ adventure.reset()
                     └─ adventure.reportState()  → AdventureState
                     (bindServices is NOT re-called)
```

### 3.4 "returns" flow

| Producer | DTO Returned | Consumer |
|----------|-------------|----------|
| `MiniAdventure.reportState()` | `AdventureState` | `AdventureManager` → `GameLoop` |
| `AdventureState.players()` | `Map<PlayerId, PlayerView>` | `GameLoop.printState()` |
| `AdventureState.outcome()` | `Outcome` | `GameLoop.printOutcome()` |
| `InputRouter.parse()` | `InputEvent` | `GameLoop` → `AdventureManager.submitInput()` |
| `AdventureRegistry.listAdventures()` | `List<EntryInfo>` | `ConsoleUI.playAdventure()` |
| `ServiceBundle.timeService()` | `Optional<GmaeTimeService>` | adventure (via `bindServices`) |
| `ServiceBundle.inventoryService()` | `Optional<GmaeInventoryService>` | adventure (via `bindServices`) |
| `ServiceBundle.questEventService()` | `Optional<GmaeQuestEventService>` | adventure (via `bindServices`) |

---

## 4. `bindServices(ServiceBundle)` — Where It Is Called

| Location | Code | Line |
|----------|------|------|
| **Definition** | `default void bindServices(ServiceBundle services) { }` | `MiniAdventure.java` |
| **Invocation** | `current.bindServices(services);` | `AdventureManager.start()` |

### Call sequence in `AdventureManager.start()`:

```java
current = registry.create(adventureId);   // 1. create fresh instance
current.init();                           // 2. one-time setup
current.bindServices(services);           // 3. inject services ◄──
return current.reportState();             // 4. return initial state
```

### When is it NOT called:

- **`restart()`** — calls `reset()` only; does not re-call `bindServices()`.
  Adventures retain service references across resets.
- **`DemoAdventure`** — does not override `bindServices()`; the default
  no-op runs silently.

### Where `ServiceBundle` originates:

- `AdventureManager(registry)` — zero-arg convenience constructor uses
  `ServiceBundle.empty()` (all services are `Optional.empty()`).
- `AdventureManager(registry, services)` — full constructor accepts a
  pre-built bundle. Used when adapters are wired in.
- Currently, `ConsoleUI.playAdventure()` calls the zero-arg constructor,
  so all adventures receive an empty bundle until adapters are integrated.

---

## 5. Dependency Layer Diagram (text)

```
┌─────────────────────────────────────────────────────┐
│                    gmae.Main                         │
│         (creates registry, registers adventures,     │
│          launches ConsoleUI)                         │
└──────┬──────────────────────────────┬────────────────┘
       │                              │
       ▼                              ▼
┌──────────────┐            ┌──────────────────┐
│   gmae.ui    │            │ gmae.adventures  │
│  ConsoleUI   │            │  DemoAdventure   │
└──────┬───────┘            └────────┬─────────┘
       │ creates                     │ implements
       ▼                             ▼
┌──────────────────────────────────────────────────────┐
│               gmae.core.engine                        │
│  AdventureRegistry  AdventureManager  GameLoop        │
│  InputRouter                                          │
└──────┬────────────────────┬──────────────────────────┘
       │ calls               │ passes
       ▼                     ▼
┌─────────────────┐  ┌───────────────────────┐
│  gmae.core.api  │  │  gmae.core.services   │
│  MiniAdventure  │  │  ServiceBundle         │
│  PlayerId       │  │  GmaeTimeService       │
│  InputEvent     │  │  GmaeInventoryService  │
│                 │  │  GmaeQuestEventService │
└────────┬────────┘  └───────────┬────────────┘
         │ returns                │ uses
         ▼                        ▼
┌──────────────────────────────────────────────────────┐
│                  gmae.core.model                      │
│  AdventureState  PlayerView  Outcome                  │
│  ItemView  TimeView  RealmView  QuestEventView        │
└──────────────────────────────────────────────────────┘
```

Arrows point from dependent → dependency. Adventures sit outside
`core.engine` and may only reach down into `core.api`, `core.model`,
and `core.services`.
