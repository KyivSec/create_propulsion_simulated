package dev.createpropulsionsimulated.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThrusterConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue BASE_THRUST;
    public static final ModConfigSpec.IntValue OBSTRUCTION_SCAN_LENGTH;
    public static final ModConfigSpec.BooleanValue REQUIRE_FUEL;
    public static final ModConfigSpec.IntValue FUEL_TANK_CAPACITY_MB;
    public static final ModConfigSpec.IntValue THRUSTER_MAX_SPEED;
    public static final ModConfigSpec.IntValue CREATIVE_THRUSTER_MAX_SPEED;
    public static final ModConfigSpec.DoubleValue FUEL_MB_PER_TICK_AT_FULL_THROTTLE;
    public static final ModConfigSpec.BooleanValue DAMAGE_ENTITIES;
    public static final ModConfigSpec.IntValue DAMAGE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue NOZZLE_OFFSET_FROM_CENTER;
    public static final ModConfigSpec.IntValue CLIENT_PARTICLES_PER_TICK;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("thruster");
        BASE_THRUST = builder.comment("Base thrust at redstone 15 and full obstruction efficiency for the standard thruster.")
                .defineInRange("baseThrust", 600.0d, 1.0d, 10_000_000.0d);
        OBSTRUCTION_SCAN_LENGTH = builder.comment("How many blocks behind the nozzle are checked for obstruction.")
                .defineInRange("obstructionScanLength", 10, 1, 64);
        REQUIRE_FUEL = builder.comment("If true, standard thrusters require turpentine fuel to produce force.")
                .define("requireFuel", true);
        FUEL_TANK_CAPACITY_MB = builder.comment("Internal fuel tank capacity in millibuckets.")
                .defineInRange("fuelTankCapacityMb", 250, 250, 64000);
        THRUSTER_MAX_SPEED = builder.comment("Standard thruster speed limit in blocks per second.")
                .defineInRange("thrusterMaxSpeed", 600, 1, 5000);
        CREATIVE_THRUSTER_MAX_SPEED = builder.comment("Creative thruster speed limit in blocks per second.")
                .defineInRange("creativeThrusterMaxSpeed", 600, 1, 1000);
        FUEL_MB_PER_TICK_AT_FULL_THROTTLE = builder.comment("Fuel consumption in millibuckets per tick at full redstone throttle.")
                .defineInRange("fuelMbPerTickAtFullThrottle", 1.0d, 0.0001d, 1000.0d);
        DAMAGE_ENTITIES = builder.comment("If true, entities inside active thruster plume are damaged.")
                .define("damageEntities", true);
        DAMAGE_TICK_INTERVAL = builder.comment("How often plume damage checks run, in ticks.")
                .defineInRange("damageTickInterval", 5, 1, 40);
        NOZZLE_OFFSET_FROM_CENTER = builder.comment("Offset from the block center where force is applied.")
                .defineInRange("nozzleOffsetFromCenter", 0.45d, 0.0d, 1.5d);
        CLIENT_PARTICLES_PER_TICK = builder.comment("Max client particles per tick while active.")
                .defineInRange("clientParticlesPerTick", 4, 0, 64);
        builder.pop();

        SPEC = builder.build();
    }

    private ThrusterConfig() {
    }
}
