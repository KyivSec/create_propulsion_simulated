# Create Propulsion: Simulated

A NeoForge 1.21.1 addon that ports only the thruster feature from Create Propulsion to the Sable + Create Aeronautics / Simulated ecosystem.

## Targets

- Minecraft: `1.21.1`
- NeoForge: `21.1.219`
- Java: `21`
- Create: `6.0.9+`
- Sable: `1.1.1+`
- Create Aeronautics: `1.1.0+`
- Simulated: `1.1.0+`

## What this addon includes

- Standard Thruster block + block entity
- Creative Thruster block + block entity (infinite fuel)
- Dedicated creative tab with both thrusters
- Create integration:
  - wrenchable blocks (`IWrenchable`)
  - goggles info (`IHaveGoggleInformation`)
  - fluid tank behaviour (`SmartFluidTankBehaviour`) for lava input
- Redstone throttle (`0..15`)
- Obstruction efficiency scan behind nozzle
- Lava fuel consumption from internal tank (standard thruster)
- Bucket-to-tank fallback fueling
- Physics integration through Sable's real force pipeline
- Force-at-point application for torque correctness
- Sub-level-safe projection for client particle emission
- Debug commands and force-vector visualization
- Upstream-inspired thruster/creative-thruster model + texture assets from Create Propulsion adapted to this mod id

## What it does not include

- VS2 or Clockwork compatibility code
- Balloons, burners, sensors, assembler, magnets, tilt systems
- Any fake compatibility layer for non-Sable physics APIs

## Force seam used

This addon uses Sable's existing force queue seam, the same propulsion lane used by propeller actors:

- `ServerSubLevel#getOrCreateQueuedForceGroup(ForceGroups.PROPULSION)`
- `QueuedForceGroup#applyAndRecordPointForce(point, impulse)`

That path applies impulse at a local point on the moving body and naturally produces torque when the force is off-center.

## Transform pattern

World/local transform logic is centralized in `SimulatedThrustAdapter`:

- resolve containing sub-level (`Sable.HELPER.getContaining`)
- transform local nozzle position to world (`logicalPose().transformPosition`)
- transform local thrust normal to world (`logicalPose().transformNormal`)

Force application remains local to the sub-level physics body, while visuals are projected to world coordinates.

## Build

```bash
gradle build
```

## Debug commands

- `/cps debug true` enables force-vector debug particles for the calling player
- `/cps debug false` disables force-vector debug particles
- `/cps scan` lists nearby thrusters and their live state values

## Torque test suite

The project includes deterministic torque tests under `src/test/java`:

- centered thruster produces zero torque
- off-center thruster produces expected non-zero torque
- symmetric multi-thruster layout cancels torque while summing linear force

## Repositories used

- [Create Maven](https://maven.createmod.net)
- [Registrate snapshots](https://maven.ithundxr.dev/snapshots)
- [RyanHCode releases](https://maven.ryanhcode.dev/releases)
- [NeoForged Maven](https://maven.neoforged.net/releases)

## Runtime dependency notes

Published artifacts for Sable, Sable Companion, Aeronautics, and Simulated are available on `maven.ryanhcode.dev/releases`, so no included-build workaround is required for this project.