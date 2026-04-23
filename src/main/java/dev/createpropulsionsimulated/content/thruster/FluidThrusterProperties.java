package dev.createpropulsionsimulated.content.thruster;

public record FluidThrusterProperties(float thrustMultiplier, float consumptionMultiplier) {
    public static final FluidThrusterProperties TURPENTINE = new FluidThrusterProperties(1.0f, 1.0f);
}
