# GMAE Adapter Test Harness

> Sub-team #2: use this harness to validate your adapters against the
> engine's service contracts without running the full game.

---

## 1. Running Tests

```bash
mvn test
```

All tests live under `src/test/java/gmae/core/services/` (contract tests)
and `src/test/java/gmae/adapters/` (adapter skeletons).

To run only the service contract tests:

```bash
mvn test -pl . -Dtest="gmae.core.services.*"
```

---

## 2. What's in the Harness

### Reference fakes (`src/test/java/gmae/core/services/fakes/`)

| Fake | Implements | Behaviour |
|------|-----------|-----------|
| `FakeTimeService` | `GmaeTimeService` | Starts at day 0 00:00; deterministic advance; two hardcoded realms ("Shadowfen" +120 min, "Crystalpeak" −180 min) |
| `FakeInventoryService` | `GmaeInventoryService` | Per-player in-memory inventory; stacks by name; deterministic ids (`P1:potion`, etc.) |
| `FakeQuestEventService` | `GmaeQuestEventService` | In-memory event list; sequential ids (`evt-1`, `evt-2`, …) |

### Contract tests (`src/test/java/gmae/core/services/`)

| Test class | Tests | Interface |
|-----------|-------|-----------|
| `InventoryContractTest` | 9 tests — add, stack, remove partial/exact/too-many, player isolation, empty default | `GmaeInventoryService` |
| `TimeContractTest` | 9 tests — initial zero, advance exact/rollover/accumulate/large, toLocal known/unknown/world-fields | `GmaeTimeService` |
| `QuestEventContractTest` | 6 tests — add returns id, list contains, remove existing/nonexistent, multi-event, empty default | `GmaeQuestEventService` |

Each contract test has a `protected createService()` method that returns
the fake implementation. Override it to inject your real adapter.

### Adapter skeletons (`src/test/java/gmae/adapters/`)

| Skeleton | Extends | Purpose |
|----------|---------|---------|
| `InventoryAdapterContractTest` | `InventoryContractTest` | Swap `createService()` → your `InventoryAdapter` |
| `TimeAdapterContractTest` | `TimeContractTest` | Swap `createService()` → your `TimeAdapter` |
| `QuestEventAdapterContractTest` | `QuestEventContractTest` | Swap `createService()` → your `QuestEventAdapter` |

These inherit **every test** from the contract test classes. When your
adapter makes all inherited tests green, it satisfies the same contract
as the reference fakes.

---

## 3. How to Validate Your Adapter

### Step-by-step (example: InventoryAdapter)

1. Implement your adapter in `src/main/java/gmae/adapters/`:

```java
package gmae.adapters;

import gmae.core.api.PlayerId;
import gmae.core.model.ItemView;
import gmae.core.services.GmaeInventoryService;
// ... import guildquest legacy types as needed ...

public class InventoryAdapter implements GmaeInventoryService {
    // wrap legacy InventoryService + Character lookups
    @Override public List<ItemView> listItems(PlayerId player) { ... }
    @Override public void addItem(PlayerId player, String name, String description, int qty) { ... }
    @Override public boolean removeItem(PlayerId player, String itemName, int qty) { ... }
}
```

2. Open `src/test/java/gmae/adapters/InventoryAdapterContractTest.java`
   and replace the `createService()` body:

```java
@Override
protected GmaeInventoryService createService() {
    return new InventoryAdapter(/* your dependencies */);
}
```

3. Run:

```bash
mvn test -Dtest="gmae.adapters.InventoryAdapterContractTest"
```

4. All 9 inherited tests should pass. If any fail, your adapter has a
   contract violation — fix the adapter, not the tests.

### Same pattern for Time and QuestEvent adapters.

---

## 4. Adding More Tests

If you discover edge cases specific to your adapter (e.g., legacy data
encoding quirks), add tests **in your adapter test class**, not in the
contract tests. The contract tests define the shared invariants; adapter
tests can extend them.

---

## 5. Import Rules (reminder)

- Contract tests import from `gmae.core.services.*` and `gmae.core.model.*` only.
- Adapter test skeletons may additionally import from `gmae.adapters.*`
  and `guildquest.*` (since they are testing the bridge layer).
- Adventure code must **never** import from `guildquest.*` or
  `gmae.adapters.*`.
