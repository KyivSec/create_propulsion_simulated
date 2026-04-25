package dev.createpropulsionsimulated.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ThrusterConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue BASE_THRUST;
    public static final ModConfigSpec.IntValue OBSTRUCTION_SCAN_LENGTH;
    public static final ModConfigSpec.BooleanValue REQUIRE_FUEL;
    public static final ModConfigSpec.IntValue FUEL_TANK_CAPACITY_MB;
    public static final ModConfigSpec.IntValue THRUSTER_MAX_SPEED;
    public static final ModConfigSpec.IntValue CREATIVE_THRUSTER_MAX_SPEED;
    public static final ModConfigSpec.IntValue ION_THRUSTER_MAX_SPEED;
    public static final ModConfigSpec.DoubleValue CREATIVE_THRUSTER_MAX_THRUST;
    public static final ModConfigSpec.DoubleValue FUEL_MB_PER_TICK_AT_FULL_THROTTLE;
    public static final ModConfigSpec.IntValue ION_THRUSTER_ENERGY_CAPACITY_FE;
    public static final ModConfigSpec.DoubleValue ION_THRUSTER_FE_PER_TICK_AT_FULL_THROTTLE;
    public static final ModConfigSpec.DoubleValue ION_THRUSTER_BASE_THRUST;
    public static final ModConfigSpec.BooleanValue DAMAGE_ENTITIES;
    public static final ModConfigSpec.IntValue DAMAGE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue NOZZLE_OFFSET_FROM_CENTER;
    public static final ModConfigSpec.BooleanValue USE_ATMOSPHERIC_PRESSURE;
    public static final ModConfigSpec.DoubleValue ATMOSPHERIC_PRESSURE_AMOUNT;
    public static final ModConfigSpec.IntValue CLIENT_PARTICLES_PER_TICK;
    public static final ModConfigSpec.DoubleValue GROUND_FRICTION_COEFFICIENT;
    public static final ModConfigSpec.DoubleValue GROUND_LINEAR_DRAG;
    public static final ModConfigSpec.DoubleValue GROUND_ROLLING_RESISTANCE;
    public static final ModConfigSpec.DoubleValue GROUNDED_SPEED_DEADZONE;
    public static final ModConfigSpec.DoubleValue GROUND_PROBE_DISTANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_PROPERTIES;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("thruster");
        BASE_THRUST = builder.comment("Base thrust at redstone 15 and full obstruction efficiency for the standard thruster.",
                        "Effective thrust uses: baseThrust * fuel_thrust_percent / 100.")
                .defineInRange("baseThrust", 600.0d, 1.0d, 10_000_000.0d);
        OBSTRUCTION_SCAN_LENGTH = builder.comment("How many blocks behind the nozzle are checked for obstruction.")
                .defineInRange("obstructionScanLength", 10, 1, 64);
        REQUIRE_FUEL = builder.comment("If true, standard thrusters require configured fluid fuel to produce force.")
                .define("requireFuel", true);
        FUEL_TANK_CAPACITY_MB = builder.comment("Internal fuel tank capacity in millibuckets.")
                .defineInRange("fuelTankCapacityMb", 250, 250, 64000);
        THRUSTER_MAX_SPEED = builder.comment("Standard thruster speed limit in blocks per second.")
                .defineInRange("thrusterMaxSpeed", 600, 1, 5000);
        CREATIVE_THRUSTER_MAX_SPEED = builder.comment("Creative thruster speed limit in blocks per second.")
                .defineInRange("creativeThrusterMaxSpeed", 10000, 1, 100000);
        ION_THRUSTER_MAX_SPEED = builder.comment("Ion thruster speed limit in blocks per second.")
                .defineInRange("ionThrusterMaxSpeed", 1000, 1, 5000);
        CREATIVE_THRUSTER_MAX_THRUST = builder.comment("Creative thruster max thrust in pN.")
                .defineInRange("creativeThrusterMaxThrust", 10_000.0d, 10.0d, 1_000_000.0d);
        FUEL_MB_PER_TICK_AT_FULL_THROTTLE = builder.comment("Fuel consumption in millibuckets per tick at full redstone throttle.")
                .defineInRange("fuelMbPerTickAtFullThrottle", 1.0d, 0.0001d, 1000.0d);
        ION_THRUSTER_ENERGY_CAPACITY_FE = builder.comment("Ion thruster internal FE capacity.")
                .defineInRange("ionThrusterEnergyCapacityFe", 1000, 1, 100000000);
        ION_THRUSTER_FE_PER_TICK_AT_FULL_THROTTLE = builder.comment("Ion thruster energy consumption in FE per tick at full redstone throttle.")
                .defineInRange("ionThrusterFePerTickAtFullThrottle", 80.0d, 0.0001d, 1000000.0d);
        ION_THRUSTER_BASE_THRUST = builder.comment("Ion thruster base thrust at redstone 15 and full obstruction efficiency.")
                .defineInRange("ionThrusterBaseThrust", 1000.d, 1.d, 10000000.d);
        DAMAGE_ENTITIES = builder.comment("If true, entities inside active thruster plume are damaged.")
                .define("damageEntities", true);
        DAMAGE_TICK_INTERVAL = builder.comment("How often plume damage checks run, in ticks.")
                .defineInRange("damageTickInterval", 5, 1, 40);
        NOZZLE_OFFSET_FROM_CENTER = builder.comment("Offset from the block center where force is applied.")
                .defineInRange("nozzleOffsetFromCenter", 0.45d, 0.0d, 1.5d);
        USE_ATMOSPHERIC_PRESSURE = builder.comment("If true, atmospheric pressure affects thruster output at altitude.")
                .define("useAtmosphericPressure", true);
        ATMOSPHERIC_PRESSURE_AMOUNT = builder.comment("Strength of atmospheric pressure influence. 1.0 = full effect, 0.0 = no effect.")
                .defineInRange("atmosphericPressureAmount", 1.0d, 0.0d, 2.0d);
        CLIENT_PARTICLES_PER_TICK = builder.comment("Max client particles per tick while active.")
                .defineInRange("clientParticlesPerTick", 4, 0, 64);
        GROUND_FRICTION_COEFFICIENT = builder.comment("Ground friction coefficient applied while a thruster detects support under it.")
                .defineInRange("groundFrictionCoefficient", 0.08d, 0.0d, 5.0d);
        GROUND_LINEAR_DRAG = builder.comment("Grounded linear drag coefficient in pN per m/s.")
                .defineInRange("groundLinearDrag", 180.0d, 0.0d, 10_000.0d);
        GROUND_ROLLING_RESISTANCE = builder.comment("Additional grounded rolling resistance in pN.")
                .defineInRange("groundRollingResistance", 80.0d, 0.0d, 10_000.0d);
        GROUNDED_SPEED_DEADZONE = builder.comment("Horizontal speed below this value is treated as stopped for grounded drag.")
                .defineInRange("groundedSpeedDeadzone", 0.03d, 0.0d, 5.0d);
        GROUND_PROBE_DISTANCE = builder.comment("How far downward a thruster probes to detect grounded support.")
                .defineInRange("groundProbeDistance", 1.5d, 0.05d, 5.0d);
        builder.push("fuelTable");
        FUEL_PROPERTIES = builder.comment(
                        "Fuel table entries as '<namespace:fluid>=<thrust_percent>,<burn_rate_percent>'.",
                        "Example: createpropulsionsimulated:turpentine=80,120"
                )
                .defineListAllowEmpty("fuelProperties", ThrusterConfig::defaultFuelProperties, () -> "",
                        value -> value instanceof String);
        builder.pop();
        builder.pop();

        SPEC = builder.build();
    }

    private ThrusterConfig() {
    }

    private static List<String> defaultFuelProperties() {
        return List.of(
                "createpropulsionsimulated:turpentine=80,120",
                "createdieselgenerators:plant_oil=55,170",
                "immersiveengineering:plantoil=55,170",
                "createdieselgenerators:ethanol=70,140",
                "immersiveengineering:ethanol=70,140",
                "mekanismgenerators:bioethanol=75,135",
                "northstar:biofuel=80,125",
                "createdieselgenerators:biodiesel=90,110",
                "immersiveengineering:biodiesel=90,110",
                "immersiveengineering:high_power_biodiesel=105,95",
                "createdieselgenerators:diesel=100,100",
                "tfmg:diesel=100,100",
                "stellaris:diesel=100,100",
                "tfmg:naphtha=95,105",
                "tfmg:kerosene=115,90",
                "createdieselgenerators:gasoline=125,80",
                "tfmg:gasoline=125,80",
                "tfmg:lpg=120,85",
                "northstar:hydrocarbon=130,75",
                "stellaris:fuel=115,100",
                "mekanism:hydrogen=120,80",
                "createaddition:bioethanol=75,135",
                "createaddition:seed_oil=55,170"
        );
    }
}
