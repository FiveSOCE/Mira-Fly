# MiraFly

Timed and permanent flight access for the Mira Minecraft plugin ecosystem.

## Download

[**Download MiraFly v0.1.0 (.jar)**](https://github.com/FiveSOCE/Mira-Fly/releases/download/v0.1.0/MiraFly-0.1.0.jar)

[View all releases](https://github.com/FiveSOCE/Mira-Fly/releases)

Current release: **v0.1.0**

## Flight behaviour

- `/fly` toggles flight.
- A normal player can only enable `/fly` if they have stored MiraFly time and the `mirafly.use` permission.
- Fly time only counts down while MiraFly flight is enabled.
- Turning `/fly` off pauses the timer.
- Logging out pauses the timer.
- When timed flight reaches zero, flight is immediately disabled.
- Players with `mirafly.permanent` can use `/fly` without consuming stored time.
- `/flytime` shows the player's current stored time, or `Permanent` for permanent-flight users.

## Fly vouchers

Each voucher is a protected **feather** worth **5 minutes / 300 seconds** of stored fly time.

Give vouchers with:

```text
/flyvoucher give <USERNAME> <Amount>
```

Right-clicking a valid voucher in the air or on a block:

1. validates the protected voucher metadata/signature,
2. adds 5 minutes to that player's existing fly-time balance,
3. tells that player their updated total fly time,
4. consumes exactly one voucher.

The visible name and lore do not establish authenticity. Forged or modified vouchers are rejected. Anvil, grindstone, smithing, crafting and enchanting modification paths are blocked for tagged MiraFly vouchers.

## Countdown messages

A timed-flight user receives private chat warnings at:

- 1 minute remaining
- 30 seconds remaining
- 10 seconds remaining

When the timer reaches zero they are told their flight time expired and flight is disabled.

## Commands

```text
/fly
/flytime
/flyvoucher give <USERNAME> <Amount>
```

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `mirafly.use` | Everyone | Allows use of stored timed fly time |
| `mirafly.permanent` | OP | Permanent `/fly` without consuming time; intended for higher ranks |
| `mirafly.admin` | OP | Allows `/flyvoucher give` |

For ranked servers, grant `mirafly.permanent` to ranks that should always have flight access.

## Data

Stored player fly-time balances are kept in:

```text
plugins/MiraFly/players.yml
```

Voucher appearance, voucher duration, messages and the generated signing secret are stored in `config.yml`.

Do not change `security.signing-secret` after vouchers have been issued, otherwise previously issued vouchers will no longer validate.

## Requirements

- Paper 1.21.11
- Java 21

## Building from source

```bash
gradle clean build
```

Output:

```text
build/libs/MiraFly-0.1.0.jar
```
