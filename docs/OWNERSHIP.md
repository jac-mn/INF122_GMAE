# GMAE Ownership Map

Ownership means that team is the **primary author**; the other team should not push changes to owned paths without prior agreement.

---

## Sub-team #1 — Engine + Contracts

These files define the engine architecture and service contracts. Changes require **sub-team #1 approval**.

| Path | Contents |
|------|----------|
| `gmae/core/**` | Engine internals, public API, DTOs, service interfaces |
| `gmae/core/api/` | `MiniAdventure`, `PlayerId`, `InputEvent` |
| `gmae/core/model/` | `AdventureState`, `PlayerView`, `Outcome`, `ItemView`, `TimeView`, `RealmView`, `QuestEventView` |
| `gmae/core/services/` | `GmaeTimeService`, `GmaeInventoryService`, `GmaeQuestEventService`, `ServiceBundle` |
| `gmae/core/engine/` | `GameLoop`, `AdventureManager`, `AdventureRegistry`, `InputRouter` |
| `gmae/ui/` | Console menus, input handling, display |
| `gmae/docs/mini_adventure_contract.md` | Frozen adventure lifecycle contract |
| `gmae/docs/services_contract.md` | Service interface specifications |
| `gmae/docs/adapter_test_harness.md` | Test harness documentation |
| `src/test/java/gmae/core/services/**` | Contract tests + reference fakes |

### Frozen after Day 2

Sub-team #2 should treat these as **read-only** after the API freeze:

- `gmae/docs/mini_adventure_contract.md`
- `gmae/docs/services_contract.md`
- All interfaces in `gmae.core.api`, `gmae.core.model`, `gmae.core.services`

---

## Sub-team #2 — Gameplay + Integration

These areas implement gameplay content and legacy integration. Sub-team #2 should **not modify engine contracts**.

| Path | Contents |
|------|----------|
| `gmae/adventures/**` | Mini-adventure implementations |
| `gmae/adapters/**` | Adapters wrapping reused `guildquest/` subsystems |
| `gmae/profiles/**` | Player profile model + persistence |
| `src/test/java/gmae/adapters/**` | Adapter contract test skeletons |

---

## Shared

| Path | Rule |
|------|------|
| `gmae/docs/**` (other files) | Both teams contribute — avoid editing the same file concurrently |
| `gmae/README.md` | Either team, with agreement |
| `gmae/docs/CONTRIBUTING.md` | Either team, with agreement |
| `gmae/docs/OWNERSHIP.md` | Either team, with agreement |

---

## Off-Limits

The following existing files must **not** be modified for the GMAE project:

- `guildquest/app/GuildQuestApp.java`
- `guildquest/store/InMemoryStore.java`
- `guildquest/domain/User.java`
- Any other file under `guildquest/**`
