package dev.createpropulsionsimulated.content.thruster;

import dev.createpropulsionsimulated.config.ThrusterConfig;
import net.minecraft.core.Direction;
import org.joml.Vector3d;

public final class ThrusterForceProvider {
    private ThrusterForceProvider() {
    }

    public static ForceSample createSample(final ThrusterBlockEntity blockEntity, final double timeStep) {
        final Direction direction = blockEntity.getFacing();

        final Vector3d directionLocal = new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        final Vector3d applicationPoint = new Vector3d(
                blockEntity.getBlockPos().getX() + 0.5d,
                blockEntity.getBlockPos().getY() + 0.5d,
                blockEntity.getBlockPos().getZ() + 0.5d
        ).fma(ThrusterConfig.NOZZLE_OFFSET_FROM_CENTER.get(), directionLocal);

        final Vector3d impulseLocal = new Vector3d(directionLocal).mul(blockEntity.getCurrentThrust() * timeStep);

        return new ForceSample(applicationPoint, impulseLocal, directionLocal);
    }

    public record ForceSample(Vector3d pointLocal, Vector3d impulseLocal, Vector3d directionLocal) {
    }
}
