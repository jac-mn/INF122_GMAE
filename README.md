# GMAE — GuildQuest Mini-Adventure Environment

## Purpose

GMAE is a new extensible mini-adventure framework supporting:

- Exactly **two local players**
- A menu of mini-adventures
- Player profiles (creation, persistence, stats)
- **2+ mini-adventures** via a defined `MiniAdventure` interface
- Integration with reused GuildQuest subsystems through **adapters** (no direct imports)

GMAE lives in `gmae/` as a separate bounded context. The existing `guildquest/` package is **not modified** by this project.

---

## Folder Structure

```
gmae/
  core/              # Engine implementation (internals hidden) — sub-team #1
    api/             # Public engine API: MiniAdventure interface, engine entry points — sub-team #1
    model/           # Engine DTOs (Coord, ItemView, EntityView, etc.) — sub-team #1
    services/        # Engine-defined service interfaces — sub-team #1
  adventures/        # Mini-adventure modules/plugins — sub-team #2
  adapters/          # Adapters wrapping reused GuildQuest subsystems — sub-team #2
  profiles/          # Player profiles + persistence — sub-team #2 (engine integrates)
  ui/                # Console UI / menu (optional GUI later) — sub-team #1
  docs/              # Design docs: UML, reuse checklist, contracts — shared
```

---

## Folder Ownership

These boundaries exist to **prevent merge conflicts** between the two sub-teams.

### Sub-team #1 — Engine + Contracts

Sub-team #1 owns the engine architecture and service contracts. Changes to these files require **sub-team #1 approval**.

| Path | Contents |
|------|----------|
| `gmae/core/**` | Engine runtime, public API, DTOs, service interfaces |
| `gmae/ui/**` | Console UI / menus |
| `gmae/docs/mini_adventure_contract.md` | Frozen adventure lifecycle contract |
| `gmae/docs/services_contract.md` | Service interface specifications |
| `gmae/docs/adapter_test_harness.md` | Test harness documentation |
| `src/test/java/gmae/core/services/**` | Contract tests + reference fakes |

### Sub-team #2 — Gameplay + Integration

Sub-team #2 owns gameplay content and legacy integration. Sub-team #2 should **not modify engine contracts**.

| Path | Contents |
|------|----------|
| `gmae/adventures/**` | Mini-adventure implementations |
| `gmae/adapters/**` | Adapters wrapping `guildquest/` subsystems |
| `gmae/profiles/**` | Player profile model + persistence |
| `src/test/java/gmae/adapters/**` | Adapter contract test skeletons |

### Shared

| Path | Rule |
|------|------|
| `gmae/docs/**` (other files) | Both teams contribute — avoid editing the same file concurrently |
| `gmae/README.md` | Either team, with agreement |

See [`docs/OWNERSHIP.md`](docs/OWNERSHIP.md) for the full table.

---

## Hard Rules

1. **Adventures MUST NOT import from `guildquest/**` directly.**
2. **Adventures MUST NOT import from `gmae/adapters/**` directly.**
3. **Adventures MUST NOT import engine internals** (e.g., `GameLoop`, `AdventureManager`, or anything in `gmae/core/` outside the three public packages listed below).
4. Adventures may **only** depend on these public API packages:
   - `gmae/core/api/**` — the `MiniAdventure` interface and engine-facing contracts
   - `gmae/core/model/**` — engine DTOs (`Coord`, `ItemView`, `EntityView`, etc.)
   - `gmae/core/services/**` — service interfaces the engine provides to adventures
5. All legacy GuildQuest integration happens in `gmae/adapters/**`, implemented by sub-team #2.
6. **No edits to `guildquest/app/GuildQuestApp.java`**, `guildquest/store/InMemoryStore.java`, or `guildquest/domain/User.java` for the final project.

---

## Engine API Freeze Policy

- The `MiniAdventure` interface and service contracts are **frozen after Day 2**.
- After freeze: only **backward-compatible additions** are allowed (new methods with defaults, new DTO fields — no signature changes).
- **Breaking changes** require a team sync + a documented reason in `gmae/docs/`.

---

## How to Run GMAE

From the repository root:

```bash
# Compile
mvn compile

# Run GMAE (separate from the legacy GuildQuest entry point)
mvn -q exec:java -Dexec.mainClass=gmae.Main
```

The console UI will display a menu of registered adventures.
Select one, enter player names, and play turn-by-turn.

### Running the legacy GuildQuest app (unchanged)

```bash
mvn -q exec:java -Dexec.mainClass=guildquest.Main
```
