# GMAE Services Contract

> **Owned by sub-team #1.** Adventures depend on these interfaces;
> sub-team #2 implements adapters that fulfill them.

---

## 1. Overview

Service interfaces live in `gmae.core.services`. They wrap legacy
`guildquest/**` capabilities behind stable, engine-DTO-only signatures
so that adventures never import legacy types.

```
Adventure  ──imports──►  gmae.core.services.*   (interfaces)
                         gmae.core.model.*       (DTOs)

Adapter    ──implements──►  gmae.core.services.* (interfaces)
           ──imports──►     guildquest.**         (legacy, hidden)
```

---

## 2. Service Interfaces

### GmaeTimeService

**Package:** `gmae.core.services`
**Source:** `src/main/java/gmae/core/services/GmaeTimeService.java`

| Method | Returns | Description |
|--------|---------|-------------|
| `nowWorld()` | `TimeView` | Current world time snapshot |
| `toLocal(String realmName)` | `Optional<TimeView>` | World time converted to the named realm's local time; empty if realm not found |
| `advanceMinutes(int minutes)` | `void` | Advances the world clock |

### GmaeInventoryService

**Package:** `gmae.core.services`
**Source:** `src/main/java/gmae/core/services/GmaeInventoryService.java`

| Method | Returns | Description |
|--------|---------|-------------|
| `listItems(PlayerId player)` | `List<ItemView>` | All items in the player's inventory |
| `addItem(PlayerId player, String name, String description, int qty)` | `void` | Adds / stacks an item |
| `removeItem(PlayerId player, String itemName, int qty)` | `boolean` | Removes quantity; returns false on failure |

### GmaeQuestEventService *(optional)*

**Package:** `gmae.core.services`
**Source:** `src/main/java/gmae/core/services/GmaeQuestEventService.java`

| Method | Returns | Description |
|--------|---------|-------------|
| `listEvents()` | `List<QuestEventView>` | All visible quest events |
| `addEvent(QuestEventView event)` | `String` | Creates event, returns generated id |
| `removeEvent(String eventId)` | `boolean` | Removes by id; returns false if not found |

Not every engine configuration will provide this service. Adventures
should check `ServiceBundle.questEventService().isPresent()` before use.

---

## 3. DTOs Used by Services

All DTOs live in `gmae.core.model`. None reference legacy types.

| DTO | Key Fields | Used By |
|-----|-----------|---------|
| `TimeView` | `days`, `hours`, `minutes`, `localTimeString` (nullable) | `GmaeTimeService` |
| `ItemView` | `id`, `name`, `description`, `quantity` | `GmaeInventoryService` |
| `RealmView` | `id`, `name`, `description` | Future realm service |
| `QuestEventView` | `id`, `title`, `startMinutes`, `endMinutes`, `realmName` | `GmaeQuestEventService` |

Source paths:

```
src/main/java/gmae/core/model/TimeView.java
src/main/java/gmae/core/model/ItemView.java
src/main/java/gmae/core/model/RealmView.java
src/main/java/gmae/core/model/QuestEventView.java
```

---

## 4. ServiceBundle & Injection Flow

**Source:** `src/main/java/gmae/core/services/ServiceBundle.java`

`ServiceBundle` holds optional references to all services. The engine
constructs one at startup and passes it to every adventure.

### Construction (engine side — `gmae.Main` or `AdventureManager`)

```java
ServiceBundle services = ServiceBundle.builder()
        .timeService(myTimeAdapter)         // may be null
        .inventoryService(myInvAdapter)     // may be null
        .questEventService(myQuestAdapter)  // may be null
        .build();
```

### Injection (engine calls this after init)

```java
adventure.init();
adventure.bindServices(services);   // ← new lifecycle step
// ... game loop ...
```

### Consumption (adventure side)

```java
@Override
public void bindServices(ServiceBundle services) {
    this.timeService = services.timeService().orElse(null);
    this.inventoryService = services.inventoryService().orElse(null);
}
```

Adventures that do not need services simply do not override
`bindServices()` — the default implementation is a no-op.

---

## 5. Lifecycle Position

`bindServices()` is called **once, between `init()` and the first
`acceptInput()`**:

```
  new Adventure()
  id() / title() / description()     ← metadata (any time)
  init()                              ← allocate state
  bindServices(bundle)                ← inject services
  ┌── game loop ──────────────────┐
  │  acceptInput()                │
  │  advance()                    │
  │  reportState()                │
  │  isComplete()                 │
  └───────────────────────────────┘
  reset()                             ← replay (bindServices NOT re-called)
```

After `reset()`, the services reference from the previous `bindServices`
call remains valid. The engine does **not** re-call `bindServices()` on
replay — adventures should retain their service references across resets.

---

## 6. Import Rules (strict)

### Adventures may import

```
gmae.core.api.*         MiniAdventure, PlayerId, InputEvent
gmae.core.model.*       AdventureState, PlayerView, Outcome,
                        ItemView, TimeView, RealmView, QuestEventView
gmae.core.services.*    GmaeTimeService, GmaeInventoryService,
                        GmaeQuestEventService, ServiceBundle
```

### Adventures must NOT import

```
guildquest.**           all legacy types
gmae.core.engine.*      GameLoop, AdventureManager, AdventureRegistry, InputRouter
gmae.adapters.**        adapter implementations
```

---

## 7. Stability Promise

These service interfaces follow the same freeze rules as `MiniAdventure`:

- **After freeze:** no existing method signatures change.
- New methods may be added with `default` implementations.
- New DTOs or new optional fields are allowed.
- Breaking changes require a full team sync + documented reason.
