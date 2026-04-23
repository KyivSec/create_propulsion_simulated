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
        final Vector3d localNozzle = new Vector3d(
                blockEntity.getBlockPos().getX() + 0.5d,
                blockEntity.getBlockPos().getY() + 0.5d,
                blockEntity.getBlockPos().getZ() + 0.5d
        ).fma(nozzleOffset, new Vector3d(exhaust.getStepX(), exhaust.getStepY(), exhaust.getStepZ()));

        final SimulatedThrustAdapter.Projection projection = SimulatedThrustAdapter.projectToWorld(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                localNozzle,
                new Vector3d(exhaust.getStepX(), exhaust.getStepY(), exhaust.getStepZ())
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
            final var particle = blockEntity.isCreative() && blockEntity.getPlumeType() == ThrusterBlockEntity.PlumeType.PLASMA
                    ? CPSParticleTypes.PLASMA.get()
                    : CPSParticleTypes.PLUME.get();

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
