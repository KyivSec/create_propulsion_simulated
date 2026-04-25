package dev.createpropulsionsimulated.content.thruster;

import dev.createpropulsionsimulated.config.ThrusterConfig;
import dev.createpropulsionsimulated.registry.CPSParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class ThrusterParticles {
    private static final float LOWEST_POWER_THRESHOLD = 5.0f / 15.0f;
    private static final float PARTICLE_VELOCITY = 4.0f;

    private ThrusterParticles() {
    }

    public static void spawn(final ThrusterBlockEntity blockEntity) {
        if (!blockEntity.shouldEmitParticles()) {
            return;
        }

        final Direction exhaust = blockEntity.getFacing().getOpposite();
        final double nozzleOffset = blockEntity.getNozzleOffsetFromCenter();
        final int stepX = exhaust.getStepX();
        final int stepY = exhaust.getStepY();
        final int stepZ = exhaust.getStepZ();
        final double baseX = blockEntity.getBlockPos().getX() + 0.5d;
        final double baseY = blockEntity.getBlockPos().getY() + 0.5d;
        final double baseZ = blockEntity.getBlockPos().getZ() + 0.5d;
        final Vector3d localNozzle = new Vector3d(
                baseX + stepX * nozzleOffset,
                baseY + stepY * nozzleOffset,
                baseZ + stepZ * nozzleOffset
        );

        final SimulatedThrustAdapter.Projection projection = SimulatedThrustAdapter.projectToWorld(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                localNozzle,
                new Vector3d(stepX, stepY, stepZ)
        );

        final Level level = projection.level();
        final Vec3 basePos = projection.position();
        final Vec3 dir = projection.direction().normalize();
        final float throttle = (float) blockEntity.getThrottle();
        final float visualPower = Math.max(throttle, LOWEST_POWER_THRESHOLD);
        final int count = Math.max(1, (int) Math.ceil(ThrusterConfig.CLIENT_PARTICLES_PER_TICK.get() * visualPower));

        for (int i = 0; i < count; i++) {
            final double px = basePos.x;
            final double py = basePos.y;
            final double pz = basePos.z;

            final double speed = PARTICLE_VELOCITY * visualPower;
            final double vx = dir.x * speed;
            final double vy = dir.y * speed;
            final double vz = dir.z * speed;
            final var particle = blockEntity.isIon()
                    ? CPSParticleTypes.ION.get()
                    : (blockEntity.isCreative()
                    ? switch (blockEntity.getPlumeType()) {
                        case PLASMA -> CPSParticleTypes.PLASMA.get();
                        case ION -> CPSParticleTypes.ION.get();
                        case PLUME, NONE -> CPSParticleTypes.PLUME.get();
                    }
                    : CPSParticleTypes.PLUME.get());

            if (level instanceof final ClientLevel clientLevel) {
                clientLevel.addParticle(particle,
                        true, px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(particle,
                        px, py, pz, vx, vy, vz);
            }
        }
    }
}
