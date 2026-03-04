# MiniAdventure Frozen Contract

> **Status: FROZEN** — signatures in this contract will not change.
> Only backward-compatible additions (new `default` methods, new optional DTO
> fields) are permitted after the freeze. Breaking changes require a full team
> sync and a documented reason.

---

## 1. Types at a Glance

| Type | Package | Role |
|------|---------|------|
| `MiniAdventure` | `gmae.core.api` | Interface every adventure implements; provides `id()`, `title()`, `description()` for registry/menu |
| `PlayerId` | `gmae.core.api` | Enum: `P1`, `P2` |
| `InputEvent` | `gmae.core.api` | Immutable action submitted by a player (`action` + optional `payload`) |
| `AdventureState` | `gmae.core.model` | Immutable snapshot returned by `reportState()` |
| `PlayerView` | `gmae.core.model` | Immutable per-player snapshot inside `AdventureState` |
| `Outcome` | `gmae.core.model` | Enum: `IN_PROGRESS`, `P1_WINS`, `P2_WINS`, `DRAW`, `COOP_WIN`, `LOSS` |

Source paths (relative to repo root):

```
src/main/java/gmae/core/api/MiniAdventure.java
src/main/java/gmae/core/api/PlayerId.java
src/main/java/gmae/core/api/InputEvent.java
src/main/java/gmae/core/model/AdventureState.java
src/main/java/gmae/core/model/PlayerView.java
src/main/java/gmae/core/model/Outcome.java
```

---

## 2. Lifecycle Call Sequence

The engine drives every adventure through the same sequence:

```
 Engine                          MiniAdventure impl
   │                                   │
   │──── new MyAdventure() ───────────►│  (constructor)
   │                                   │
   │  ── registration / menu ────────  │
   │  id()       ◄───────────────────►│  stable unique key  (e.g. "treasure-hunt")
   │  title()    ◄───────────────────►│  display name       (e.g. "Treasure Hunt")
   │  description() ◄────────────────►│  menu summary text
   │                                   │
   │──── init() ──────────────────────►│  set up board, allocate state
   │──── bindServices(bundle) ───────►│  inject engine services (optional)
   │                                   │
   │  ┌─── game loop ────────────────┐ │
   │  │                              │ │
   │  │  acceptInput(P1, event) ────►│ │  buffer P1 action
   │  │  acceptInput(P2, event) ────►│ │  buffer P2 action  (0-N per step)
   │  │  advance() ─────────────────►│ │  process buffered input, update state
   │  │  reportState() ◄────────────►│ │  return AdventureState snapshot
   │  │  isComplete() ◄─────────────►│ │  return true → exit loop
   │  │                              │ │
   │  └──────────────────────────────┘ │
   │                                   │
   │──── reset() ─────────────────────►│  restore to post-init() state
   │  (loop again or discard)          │
```

### Metadata methods

| Method | Returns | Called when |
|--------|---------|------------|
| `id()` | Stable unique `String` (e.g. `"treasure-hunt"`) | Registration, persistence, lookup — must never change between versions |
| `title()` | Human-readable display name | Adventure selection menu |
| `description()` | One-line summary | Shown below title in menu |

These may be called at any time (before `init()`, between games, etc.)
and must always return the same values for a given adventure class.

### Call ordering guarantees

1. `id()`, `title()`, and `description()` may be called at any time.
2. `init()` is called exactly once before the first game loop iteration.
3. `bindServices(bundle)` is called once, after `init()` and before the
   first `acceptInput()`. It is **not** re-called on `reset()`.
   Adventures should retain service references across resets.
   (Default implementation is a no-op — backward-compatible addition.)
4. Within each step: `acceptInput` calls come before `advance()`.
5. `advance()` is called exactly once per step.
6. `reportState()` is called after `advance()` returns.
7. `isComplete()` is checked after `reportState()`.
8. `reset()` may be called at any time after `init()`. After `reset()`,
   the object behaves as if `init()` was just called (service bindings
   remain active).

---

## 3. Input Model

### InputEvent

```java
new InputEvent("MOVE", "NORTH")   // action with payload
InputEvent.of("PASS")             // action without payload
InputEvent.of("USE_ITEM", "Potion")
```

- `action` (String, non-null): identifies the action type.
  Each adventure defines its own action vocabulary.
- `payload` (String, nullable): optional context data.

### Two-player input

- Every `acceptInput` call is tagged with `PlayerId.P1` or `PlayerId.P2`.
- **Turn-based mode:** the engine sends input only for the active player,
  then calls `advance()`.
- **Tick-based mode:** the engine may send input for both players (or
  neither) before each `advance()` call.
- Adventures must handle receiving zero inputs in a step (a no-op advance).
- Adventures must handle unrecognised action strings gracefully (ignore
  or produce a message — never throw).

---

## 4. AdventureState — Minimum Contents

Every `reportState()` return value must include:

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | `String` | yes | Same value as `MiniAdventure.title()` |
| `tickOrTurn` | `int` | yes | Monotonically increasing (0 at init, incremented by `advance()`) |
| `messages` | `List<String>` | yes (may be empty) | Events since the last snapshot, in order |
| `players` | `Map<PlayerId, PlayerView>` | yes | Must contain entries for both `P1` and `P2` |
| `complete` | `boolean` | yes | `true` when the adventure has ended |
| `outcome` | `Outcome` | yes | `IN_PROGRESS` while `complete` is `false` |

Built using the provided builder:

```java
AdventureState.builder()
    .title("Treasure Hunt")
    .tickOrTurn(currentTurn)
    .messages(List.of("P1 moved north", "P1 found a key"))
    .players(Map.of(
        PlayerId.P1, new PlayerView(PlayerId.P1, "Alice", 10),
        PlayerId.P2, new PlayerView(PlayerId.P2, "Bob",    5)
    ))
    .complete(false)
    .outcome(Outcome.IN_PROGRESS)
    .build();
```

### PlayerView

| Field | Type | Notes |
|-------|------|-------|
| `id` | `PlayerId` | Which player |
| `name` | `String` | Display name |
| `score` | `int` | Adventure-defined meaning |
| `attributes` | `Map<String, String>` | Open extension point for adventure-specific data (e.g. `"hp"→"100"`, `"position"→"3,7"`) |

---

## 5. Outcome / Winner Semantics

### Competitive adventures

Use `P1_WINS`, `P2_WINS`, or `DRAW`.

| Situation | `complete` | `outcome` |
|-----------|-----------|-----------|
| Game ongoing | `false` | `IN_PROGRESS` |
| Player 1 wins | `true` | `P1_WINS` |
| Player 2 wins | `true` | `P2_WINS` |
| Tie | `true` | `DRAW` |

### Cooperative adventures

Use `COOP_WIN` or `LOSS`.

| Situation | `complete` | `outcome` |
|-----------|-----------|-----------|
| Game ongoing | `false` | `IN_PROGRESS` |
| Both players succeed | `true` | `COOP_WIN` |
| Both players fail | `true` | `LOSS` |

### Mixed modes

An adventure may use any subset of `Outcome` values as long as
`IN_PROGRESS` is used while `complete == false` and a terminal value
is used when `complete == true`. The two must be consistent:
`isComplete()` must return `true` if and only if `outcome != IN_PROGRESS`.

---

## 6. Reset Semantics

- `reset()` restores the adventure to the exact state it was in
  immediately after `init()` completed.
- All internal state (board, scores, turn counter, input buffer, RNG seed)
  must be restored.
- After `reset()`, the engine may run the game loop again without calling
  `init()` a second time.
- `reset()` must be safe to call even if the adventure is still
  `IN_PROGRESS` (early quit / restart).

---

## 7. Determinism Expectation

Given the same sequence of `acceptInput` calls, repeated runs of
`advance()` should produce the same state. Adventures that use
randomness should:

1. Seed a local `java.util.Random` in `init()`.
2. Re-seed with the same seed in `reset()`.
3. Never use `Math.random()` or shared static RNG.

This enables future replay/testing support without changing the contract.

---

## 8. What Adventures Must NOT Import

Adventures may only depend on:

```
gmae.core.api.*        (MiniAdventure, PlayerId, InputEvent)
gmae.core.model.*      (AdventureState, PlayerView, Outcome)
gmae.core.services.*   (service interfaces — when defined)
```

**Banned imports from adventure code:**

- `guildquest.**` — all legacy types
- `gmae.core` internal classes (e.g. `GameLoop`, `AdventureManager`)
- `gmae.adapters.**` — adapter layer

All GuildQuest reuse is handled by adapters, which are injected through
`gmae.core.services` interfaces. Adventures never see the adapters directly.

---

## 9. Stability Promise

After the API freeze (Day 2):

- **No existing method signatures will change** in `MiniAdventure`,
  `InputEvent`, `AdventureState`, `PlayerView`, `PlayerId`, or `Outcome`.
- New methods on `MiniAdventure` must have `default` implementations.
- New fields on DTOs must be optional or have sensible defaults.
- Breaking changes require a full team sync, approval from both
  sub-teams, and a written justification in `gmae/docs/`.
