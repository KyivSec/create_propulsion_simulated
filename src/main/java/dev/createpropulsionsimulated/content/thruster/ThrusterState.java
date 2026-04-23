package dev.createpropulsionsimulated.content.thruster;

public final class ThrusterState {
    public static final int FULL_EFFICIENCY_AIR_GAP = 10;

    private ThrusterState() {
    }

    public static double throttle(final int redstonePower) {
        return Math.clamp(redstonePower / 15.0d, 0.0d, 1.0d);
    }

    public static double obstructionEfficiency(final int unobstructedBlocks) {
        final int clamped = Math.clamp(unobstructedBlocks, 0, FULL_EFFICIENCY_AIR_GAP);
        return clamped / (double) FULL_EFFICIENCY_AIR_GAP;
    }

    public static boolean canProduceThrust(final boolean powered, final boolean hasFuel, final boolean requireFuel) {
        return powered && (!requireFuel || hasFuel);
    }

    public static double thrust(final double baseThrust, final double throttle, final double obstructionEfficiency) {
        return baseThrust * throttle * obstructionEfficiency;
    }
}
