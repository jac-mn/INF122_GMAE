# GMAE Contributing Guide

---

## Engine API Freeze

The following APIs are **frozen after Day 2**:

```
gmae.core.api.*         MiniAdventure, PlayerId, InputEvent
gmae.core.model.*       AdventureState, PlayerView, Outcome,
                        ItemView, TimeView, RealmView, QuestEventView
gmae.core.services.*    GmaeTimeService, GmaeInventoryService,
                        GmaeQuestEventService, ServiceBundle
```

After the freeze, **no existing method signatures or field types may change**.

Allowed after freeze:
- New methods on interfaces — must have `default` implementations
- New fields on DTOs — must be optional or have sensible defaults
- New interfaces or DTOs — no restrictions

**Breaking changes** require:
1. Team discussion with both sub-teams
2. A documented reason in `gmae/docs/`
3. Updates to both contract docs and contract tests

---

## Import Rules

### Adventure code may ONLY import

```
gmae.core.api.*         MiniAdventure, PlayerId, InputEvent
gmae.core.model.*       AdventureState, PlayerView, Outcome, ItemView,
                        TimeView, RealmView, QuestEventView
gmae.core.services.*    GmaeTimeService, GmaeInventoryService,
                        GmaeQuestEventService, ServiceBundle
```

### Adventure code must NOT import

```
guildquest.**           Legacy types (all packages)
gmae.core.engine.**     Engine internals (GameLoop, AdventureManager,
                        AdventureRegistry, InputRouter)
gmae.adapters.**        Adapter implementations
```

All legacy GuildQuest integration is handled by adapters in
`gmae/adapters/**`. Adventures access legacy capabilities only through
the service interfaces in `gmae.core.services`.

---

## Registering Adventures

All new adventures must:

1. **Implement `MiniAdventure`** from `gmae.core.api`
2. **Reside in** `src/main/java/gmae/adventures/<name>/`
   (e.g. `gmae/adventures/treasurehunt/TreasureHuntAdventure.java`)
3. **Be registered** in `gmae.Main` (or a future registry config) using
   `AdventureRegistry`:

```java
registry.register(TreasureHuntAdventure::new);
```

4. **Import only** from the allowed packages listed above
5. **Override `bindServices()`** if the adventure needs time, inventory,
   or quest-event services

---

## Adapter Development

Sub-team #2 implements adapters in `src/main/java/gmae/adapters/` that
bridge `gmae.core.services` interfaces to the legacy `guildquest`
subsystems.

### Validating adapters

Use the contract test harness:

```bash
mvn test
```

Each service has a ready-made adapter test skeleton in
`src/test/java/gmae/adapters/`:

| Skeleton | Override `createService()` with |
|----------|-------------------------------|
| `InventoryAdapterContractTest` | Your `InventoryAdapter` |
| `TimeAdapterContractTest` | Your `TimeAdapter` |
| `QuestEventAdapterContractTest` | Your `QuestEventAdapter` |

Adapters should **pass all inherited contract tests**. If a test fails,
fix the adapter — do not modify the contract tests.

See [`adapter_test_harness.md`](adapter_test_harness.md) for full
step-by-step instructions.

---

## Stability Principle

**Prefer adding over modifying.**

- Add new interfaces rather than changing existing ones.
- Add new optional DTO fields rather than changing field types.
- Add new `default` methods rather than changing existing signatures.

This keeps both sub-teams unblocked and avoids merge conflicts.

---

## Branch Naming

Use these prefixes so it is clear which area a branch touches:

| Prefix | Area | Owner |
|--------|------|-------|
| `feature/gmae-core-*` | Engine core, model, services | Sub-team #1 |
| `feature/gmae-ui-*` | Console UI / menus | Sub-team #1 |
| `feature/adventure-*` | Mini-adventure modules | Sub-team #2 |
| `feature/adapters-*` | Adapter layer | Sub-team #2 |
| `feature/profiles-*` | Player profiles | Sub-team #2 |
| `fix/gmae-*` | Bug fixes (any area) | Whoever owns the area |
| `docs/gmae-*` | Documentation only | Either team |

---

## Pull Request Rules

1. **Do not mix engine + adventure changes in one commit series** if
   avoidable. Keep engine PRs and adventure PRs separate so reviews
   stay focused and merge conflicts stay isolated.

2. **No edits to existing `guildquest/**` files.** The legacy codebase
   must remain intact for earlier assignments. If you need legacy
   functionality, write an adapter in `gmae/adapters/`.

3. **Respect ownership boundaries.** If your PR touches files owned by
   the other sub-team, get their review before merging.

4. **Run `mvn test` before opening a PR.** All 54+ contract tests must
   pass.

---

## Commit Messages

No strict format is enforced, but a short scope prefix is helpful:

```
[core] Add MiniAdventure interface
[adventure] Implement treasure-hunt adventure
[adapter] Wrap InventoryService for engine use
[profiles] Add profile save/load
[ui] Add adventure selection menu
[docs] Update API contract
```
