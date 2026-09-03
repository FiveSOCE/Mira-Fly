# MiraFly

MiraFly is the timed, permanent and faction-aware flight controller for the Mira Paper server suite. It owns the live Bukkit flight state, tracks stored flight time, supports secure flight vouchers and integrates with MiraFactions territory/upgrade rules.

## Download

[**Download MiraFly v0.1.1**](https://github.com/FiveSOCE/Mira-Fly/releases/download/v0.1.1/MiraFly-0.1.1.jar)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- MiraFactions v0.2.7+ optional; required only for `/f fly` faction-flight integration

## How MiraFly Works

Normal players can toggle `/fly` only when they have stored MiraFly time and `mirafly.use`. Stored time counts down only while the player is actually flying. Turning flight off, standing on the ground, logging out or entering a blocked region pauses the timer. At 1 minute, 30 seconds and 10 seconds remaining the player receives private warnings; at zero, timed flight is disabled immediately.

Players with `mirafly.permanent` can use `/fly` without consuming stored time. `/flytime` reports the current balance or `Permanent` for permanent users. Player balances persist in `plugins/MiraFly/players.yml`.

Flight vouchers are protected feather items, worth 5 minutes/300 seconds by default. Right-clicking a valid voucher verifies its hidden metadata/signature, adds the configured time to the redeemer's balance, reports the new total and consumes one voucher. Forged or modified vouchers are rejected, and common item-modification paths such as anvils, grindstones, smithing, crafting and enchanting are blocked for tagged vouchers. The signing secret in `config.yml` must remain stable after vouchers have been issued.

When MiraFactions is installed, MiraFactions decides whether the faction/rank has entitlement while MiraFly remains the runtime flight authority. `/f fly` requires faction membership, the FLIGHT upgrade, faction FLY rank permission and an allowed current territory. MiraFly continuously re-checks territory and disables faction flight when the player leaves an allowed area. Region configuration can separately control blocked worlds and allowed personal/faction territory types.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/fly` | `mirafly.use` or `mirafly.permanent` | Toggles personal MiraFly flight. Timed users require stored time; permanent users do not consume time. |
| `/flytime` | None beyond normal access | Shows remaining stored flight time or `Permanent`. |
| `/flyvoucher give <player> [amount]` | `mirafly.admin` | Gives one or more secure MiraFly vouchers to a player. |
| `/f fly` | MiraFactions-managed | Toggles faction flight through MiraFactions when the faction upgrade/rank/territory requirements are met. |

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirafly.use` | Everyone | Allows use of timed flight when stored time is available. |
| `mirafly.permanent` | OP | Grants permanent `/fly` without consuming stored time. |
| `mirafly.admin` | OP | Allows administrative voucher giving. |

Faction-flight rank permissions are owned by MiraFactions rather than MiraFly.
