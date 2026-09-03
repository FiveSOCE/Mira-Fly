# MiraFly

Timed, permanent and faction-aware flight access for the Mira Minecraft plugin ecosystem.

## Download

[**Download MiraFly v0.1.1 (.jar)**](https://github.com/FiveSOCE/Mira-Fly/releases/download/v0.1.1/MiraFly-0.1.1.jar)

[View all releases](https://github.com/FiveSOCE/Mira-Fly/releases)

Current release: **v0.1.1**

## Flight behaviour

- `/fly` toggles timed or permanent MiraFly flight.
- A normal player can only enable `/fly` if they have stored MiraFly time and the `mirafly.use` permission.
- Fly time only counts down while the player is actually flying.
- Turning `/fly` off, standing on the ground, logging out or entering a blocked region pauses the timer.
- When timed flight reaches zero, MiraFly immediately disables that flight source.
- Players with `mirafly.permanent` can use `/fly` without consuming stored time.
- `/flytime` shows the player's current stored time, or `Permanent` for permanent-flight users.
- MiraFly is the runtime authority for faction flight when MiraFactions v0.2.7+ is installed.

## MiraFactions flight integration

MiraFactions owns faction entitlement while MiraFly owns the actual Bukkit flight state.

`/f fly` checks that:

1. the player belongs to a faction,
2. the faction has unlocked the FLIGHT upgrade,
3. the player's faction rank has FLY permission,
4. MiraFly allows flight in the current territory.

The default faction-flight territory policy allows:

```text
OWN
ALLY
```

MiraFly re-checks the player's territory continuously. Leaving an allowed faction-flight territory disables faction flight instead of allowing MiraFactions and MiraFly to compete over `allowFlight`.

## Region-aware restrictions

Configure `regions` in `config.yml`.

```yaml
regions:
  blocked-worlds: []
  personal-allowed-territories:
    - SAFEZONE
    - WILDERNESS
    - OWN
    - ALLY
    - TRUCE
    - NEUTRAL
  faction-allowed-territories:
    - OWN
    - ALLY
```

Available MiraFactions territory values are `SAFEZONE`, `WARZONE`, `WILDERNESS`, `OWN`, `ALLY`, `TRUCE`, `NEUTRAL` and `ENEMY`.

Without MiraFactions installed, ordinary `/fly` still works and only `blocked-worlds` is enforced. Faction flight naturally requires MiraFactions.

## Fly vouchers

Each voucher is a protected **feather** worth **5 minutes / 300 seconds** of stored fly time.

Give vouchers with:

```text
/flyvoucher give <USERNAME> <Amount>
```

Right-clicking a valid voucher in the air or on a block validates the protected voucher metadata/signature, adds five minutes to that player's stored balance, reports the updated total and consumes exactly one voucher.

The visible name and lore do not establish authenticity. Forged or modified vouchers are rejected. Anvil, grindstone, smithing, crafting and enchanting modification paths are blocked for tagged MiraFly vouchers.

## Countdown messages

A timed-flight user receives private chat warnings at:

- 1 minute remaining
- 30 seconds remaining
- 10 seconds remaining

When the timer reaches zero they are told their flight time expired and timed flight is disabled.

## Commands

```text
/fly
/flytime
/flyvoucher give <USERNAME> <Amount>
```

Faction flight is toggled through MiraFactions:

```text
/f fly
```

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `mirafly.use` | Everyone | Allows use of stored timed fly time |
| `mirafly.permanent` | OP | Permanent `/fly` without consuming time |
| `mirafly.admin` | OP | Allows `/flyvoucher give` |

Faction-flight rank permission remains managed by MiraFactions.

## Data

Stored player fly-time balances are kept in:

```text
plugins/MiraFly/players.yml
```

Voucher appearance, voucher duration, region policies, messages and the generated signing secret are stored in `config.yml`.

Do not change `security.signing-secret` after vouchers have been issued, otherwise previously issued vouchers will no longer validate.

## Requirements

- Paper 1.21.11
- Java 21
- MiraFactions v0.2.7+ optional, required only for `/f fly` integration

## Building from source

```bash
gradle clean build
```

Output:

```text
build/libs/MiraFly-0.1.1.jar
```
