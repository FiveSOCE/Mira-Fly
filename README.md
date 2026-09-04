# MiraFly

MiraFly is the timed, permanent and faction-aware flight controller for the Mira Paper server suite. It owns live Bukkit flight state, stored flight time, secure fly vouchers and MiraFactions territory/upgrade policy while delegating flight visuals to MiraCosmetics.

## Download

[**Download MiraFly v0.1.2**](https://github.com/FiveSOCE/Mira-Fly/releases/download/v0.1.2/MiraFly-0.1.2.jar)

[View All Releases](https://github.com/FiveSOCE/Mira-Fly/releases)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- MiraCore 0.2.0 or newer
- MiraFactions 0.2.8+ optional; required only for faction flight
- MiraCosmetics 0.1.1+ optional/recommended for flight effects

## Flight Ownership

MiraFly remains the only authority that turns timed/permanent/faction flight on and off. MiraFactions decides faction entitlement/territory policy for `/f fly`, then delegates the actual Bukkit flight state to MiraFly.

Timed flight balance is consumed only while the player is actually flying under personal timed flight. Time is paused while grounded, logged out, blocked by region policy, using permanent flight, or covered by valid faction flight.

Warnings remain at 60, 30 and 10 seconds, with immediate disable at zero.

## MiraCosmetics Integration

v0.1.2 delegates all continuous flight particles to MiraCosmetics.

MiraFly calls the registered Cosmetics API only when:

- the player is actually flying
- the flight is currently requested and valid through MiraFly
- personal/faction territory policy allows it
- the player is not simply flying because they are in Creative or Spectator

The default call interval is 5 ticks. MiraCosmetics performs the final visual throttle and decides which FLY cosmetic/effect the player has equipped.

No particle definitions are duplicated in MiraFly.

## Vouchers and Stored Time

Fly vouchers remain signed feather items worth 300 seconds by default. Normal voucher validation/protection remains unchanged.

Stored-time addition is now overflow-safe. External API grants cannot wrap a very large positive balance into a zero/negative value.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/fly` | `mirafly.use` or `mirafly.permanent` | Toggles personal MiraFly flight. |
| `/flytime` | Normal access | Shows stored fly time or Permanent. |
| `/flyvoucher give <player> [amount]` | `mirafly.admin` | Gives secure MiraFly vouchers. |
| `/f fly` | MiraFactions-managed | Toggles faction flight through MiraFactions entitlement/territory rules. |

## API / Core Integration

`MiraFlyApi` is registered through Bukkit ServicesManager and MiraCore. It exposes remaining stored time, safe positive time grants, active personal/faction runtime state and programmatic personal flight toggle.

MiraFly registers module health in MiraCore and audits personal/faction toggle state plus timed-flight expiry.

## Configuration

`config.yml` retains voucher, signing, region and message controls and adds `cosmetics.flight-effect-interval-ticks`.

## Building

```bash
gradle clean build
```

The output JAR is created in `build/libs/`.
