# MiraFly

## Downloads by server version

| Paper server | Java | JAR filename | Download |
| --- | --- | --- | --- |
| **1.18.2** | 17 | `MiraFly_1.18.2.jar` | [Download 1.18.2 JAR](https://github.com/FiveSOCE/Mira-Fly/releases/download/dev-1.18.2/MiraFly_1.18.2.jar) |
| **1.21.11–26.2** | Java 21 baseline, 26.2 may require Java 25 | `MiraFly_1.21.11-26.2.jar` | [Download modern JAR](https://github.com/FiveSOCE/Mira-Fly/releases/download/compat-modern-1.21.11-26.2/MiraFly_1.21.11-26.2.jar) |

Both named JARs have been published following successful GitHub Actions builds. Compilation checks do not replace live testing, and **1.19–1.21.10 are not certified**. Install only the JAR matching your server.

[Legacy source](https://github.com/FiveSOCE/Mira-Fly/tree/compat/1.18.2) · [Modern source](https://github.com/FiveSOCE/Mira-Fly/tree/main) · [All releases](https://github.com/FiveSOCE/Mira-Fly/releases)

MiraFly is the timed, permanent and faction-aware flight controller. It manages flight permission, countdowns, protected fly vouchers, persisted fly balance and faction entitlement, while delegating visual flight cosmetics where available.

## Dependencies and integration

- **Required:** Matching Paper and Java runtime and a matching **MiraCore** build.
- **Optional:** Matching MiraFactions for faction/territory flight (`/f fly`); matching MiraCosmetics for flight visual effects.
- **Economy / Vault:** Not required.
- Before switching builds, back up `plugins/MiraFly/` and remove earlier MiraFly JARs.

## Commands and permissions

| Command | Permission | Function |
| --- | --- | --- |
| `/fly` | `mirafly.use` or `mirafly.permanent` | Toggle personal flight when eligible |
| `/flytime` | Default access | Show remaining time or permanent status |
| `/flyvoucher give <player> [amount]` | `mirafly.admin` | Give signed flight vouchers |
| `/f fly` | MiraFactions policy | Toggle faction flight when MiraFactions is installed |

| Permission | Default | Purpose |
| --- | --- | --- |
| `mirafly.use` | Everyone | Spend stored flight time |
| `mirafly.permanent` | OP | Fly without consuming time |
| `mirafly.admin` | OP | Issue vouchers |

**Legacy voucher note:** Paper 1.18.2 does not expose `PrepareGrindstoneEvent`; voucher result-slot protection remains implemented via `InventoryClickEvent` and requires live testing to verify bypass resistance.

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
