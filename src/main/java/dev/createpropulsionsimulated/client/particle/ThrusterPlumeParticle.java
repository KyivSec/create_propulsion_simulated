package dev.createpropulsionsimulated.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ThrusterPlumeParticle extends SimpleAnimatedParticle {
    private static final float PLUME_SPREAD = 0.05f;
    private static final float PLUME_BASE_QUAD_SIZE = 2.0f;
    private static final float PLUME_FRICTION = 0.99f;
    private static final float PLUME_SPEED_MULTIPLIER = 0.144f;
    private static final int PLUME_BASE_LIFETIME = 40;
    private static final float SMOKE_SPREAD_MAGNITUDE = 0.15f;
    private static final float SMOKE_FRICTION = 0.96f;
    private static final float SMOKE_BASE_LIFT = 0.02f;

    private static final float COLLISION_SPEED_RETENTION = 0.9f;
    private static final double COLLISION_DETECTION_EPSILON = 0.001;
    private static final float COLLISION_PERPENDICULAR_DAMPEN = 0.1f;

    private static final int PLUME_SPRITE_COUNT = 6;
    private static final int SMOKE_SPRITE_COUNT = 7;
    private static final int BASE_SMOKE_TRANSITION_AGE = 20;

    private enum ParticleState {
        PLUME, SMOKE
    }

    private final SpriteSet spriteSet;
    private ParticleState currentState;
    private final int smokeTransitionAge;
    private final float smokeLift;
    private final Vec3 spreadDirection;
    private final float spreadMagnitude;

    private float currentSpeedMultiplier;
    private float currentFriction;
    private float baseSize;

    private boolean hasCollided;
    private double dx;
    private double dy;
    private double dz;

    protected ThrusterPlumeParticle(final ClientLevel level,
                                    final double x,
                                    final double y,
                                    final double z,
                                    final double dxSource,
                                    final double dySource,
                                    final double dzSource,
                                    final SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet, 0);
        this.spriteSet = spriteSet;

        this.quadSize *= PLUME_BASE_QUAD_SIZE;
        this.baseSize = this.quadSize;
        this.lifetime = PLUME_BASE_LIFETIME + random.nextInt(5);
        this.friction = PLUME_FRICTION;

        this.dx = dxSource + getRandomSpread();
        this.dy = dySource + getRandomSpread();
        this.dz = dzSource + getRandomSpread();

        this.hasPhysics = true;
        this.currentSpeedMultiplier = PLUME_SPEED_MULTIPLIER;
        this.currentFriction = PLUME_FRICTION;
        this.currentState = ParticleState.PLUME;

        final Vec3 initialVel = new Vec3(this.dx, this.dy, this.dz).normalize();
        Vec3 nonParallel = new Vec3(1, 0, 0);
        if (Math.abs(initialVel.dot(nonParallel)) > 0.99) {
            nonParallel = new Vec3(0, 1, 0);
        }
        final Vec3 u = initialVel.cross(nonParallel).normalize();
        final Vec3 v = initialVel.cross(u).normalize();
        final double randomAngle = this.random.nextDouble() * 2.0 * Math.PI;
        this.spreadDirection = u.scale(Math.cos(randomAngle)).add(v.scale(Math.sin(randomAngle)));

        this.spreadMagnitude = 0.1f + this.random.nextFloat() * 0.7f;
        this.smokeTransitionAge = BASE_SMOKE_TRANSITION_AGE + this.random.nextIntBetweenInclusive(-2, 2);
        this.smokeLift = SMOKE_BASE_LIFT - 0.01f + this.random.nextFloat() * 0.04f;

        setSpriteFromAge(this.spriteSet);
        setColor(0xFFFFFF);
        setAlpha(1.0f);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        final double collisionIgnoreDotThreshold = -1.0E-5D;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        final double intendedMoveX = this.dx * this.currentSpeedMultiplier;
        final double intendedMoveY = this.dy * this.currentSpeedMultiplier;
        final double intendedMoveZ = this.dz * this.currentSpeedMultiplier;

        final double prevX = this.x;
        final double prevY = this.y;
        final double prevZ = this.z;

        this.move(intendedMoveX, intendedMoveY, intendedMoveZ);

        final double actualMoveX = this.x - prevX;
        final double actualMoveY = this.y - prevY;
        final double actualMoveZ = this.z - prevZ;

        boolean collisionDetected = false;
        Vec3 collisionNormal = null;

        if (this.onGround) {
            collisionDetected = true;
            collisionNormal = new Vec3(0, 1, 0);
        } else {
            final float factor = 0.95f;
            final boolean blockedX = Math.abs(intendedMoveX) > COLLISION_DETECTION_EPSILON && Math.abs(actualMoveX) < Math.abs(intendedMoveX) * factor;
            final boolean blockedZ = Math.abs(intendedMoveZ) > COLLISION_DETECTION_EPSILON && Math.abs(actualMoveZ) < Math.abs(intendedMoveZ) * factor;
            final boolean blockedYCeiling = Math.abs(intendedMoveY) > COLLISION_DETECTION_EPSILON && intendedMoveY > 0 && Math.abs(actualMoveY) < Math.abs(intendedMoveY) * factor;

            if (blockedYCeiling) {
                collisionDetected = true;
                collisionNormal = new Vec3(0, -1, 0);
            } else if (blockedX) {
                collisionDetected = true;
                collisionNormal = new Vec3(intendedMoveX < 0 ? 1 : -1, 0, 0);
            } else if (blockedZ) {
                collisionDetected = true;
                collisionNormal = new Vec3(0, 0, intendedMoveZ < 0 ? 1 : -1);
            }
        }

        if (collisionDetected && collisionNormal != null) {
            final Vec3 incomingVel = new Vec3(this.dx, this.dy, this.dz);
            if (incomingVel.normalize().dot(collisionNormal) <= collisionIgnoreDotThreshold) {
                this.hasCollided = true;
                final double incomingSpeedSq = incomingVel.lengthSqr();

                if (incomingSpeedSq > 1e-7) {
                    final Vec3 incomingVelNormalized = incomingVel.normalize();
                    final double dot = incomingVelNormalized.dot(collisionNormal);
                    final double angleOfIncidence = Math.acos(org.joml.Math.clamp(Math.abs(dot), 0.0, 1.0));
                    final float spreadBlendFactor = (float) Math.cos(angleOfIncidence);
                    final float slideBlendFactor = (float) Math.sin(angleOfIncidence);

                    final Vec3 vNormal = collisionNormal.scale(incomingVel.dot(collisionNormal));
                    final Vec3 vTangential = incomingVel.subtract(vNormal);

                    final Vec3 desiredNormal = incomingVel.dot(collisionNormal) < 0 ? vNormal.scale(-COLLISION_PERPENDICULAR_DAMPEN) : vNormal;

                    Vec3 axis1;
                    if (Math.abs(collisionNormal.y) > 0.9) {
                        axis1 = new Vec3(1, 0, 0).normalize();
                    } else {
                        axis1 = new Vec3(0, 1, 0).normalize();
                    }
                    Vec3 axis2 = collisionNormal.cross(axis1).normalize();
                    if (axis2.lengthSqr() < 0.1) {
                        axis1 = Math.abs(collisionNormal.x) > 0.9 ? new Vec3(0, 0, 1).normalize() : new Vec3(1, 0, 0).normalize();
                        axis2 = collisionNormal.cross(axis1).normalize();
                    }

                    final double randomAngle = this.random.nextDouble() * Math.PI * 2.0D;
                    final Vec3 spreadPlaneDir = axis1.scale(Math.cos(randomAngle)).add(axis2.scale(Math.sin(randomAngle))).normalize();

                    final Vec3 spreadComponent = spreadPlaneDir.scale(incomingVel.length() * spreadBlendFactor);
                    final Vec3 slideComponent = vTangential.scale(slideBlendFactor);
                    final Vec3 desiredTangential = slideComponent.add(spreadComponent);

                    final Vec3 newVel = desiredNormal.add(desiredTangential);
                    final double newVelMagnitude = newVel.length();
                    if (newVelMagnitude > 1e-5) {
                        this.dx = (newVel.x / newVelMagnitude) * incomingVel.length() * COLLISION_SPEED_RETENTION;
                        this.dy = (newVel.y / newVelMagnitude) * incomingVel.length() * COLLISION_SPEED_RETENTION;
                        this.dz = (newVel.z / newVelMagnitude) * incomingVel.length() * COLLISION_SPEED_RETENTION;
                    } else {
                        this.dx = spreadPlaneDir.x * incomingVel.length() * COLLISION_SPEED_RETENTION * 0.5;
                        this.dy = spreadPlaneDir.y * incomingVel.length() * COLLISION_SPEED_RETENTION * 0.5;
                        this.dz = spreadPlaneDir.z * incomingVel.length() * COLLISION_SPEED_RETENTION * 0.5;
                    }
                } else {
                    this.dx *= 0.1;
                    this.dy *= 0.1;
                    this.dz *= 0.1;
                }
            }
        }

        if (this.currentState == ParticleState.PLUME && this.age >= this.smokeTransitionAge) {
            this.currentState = ParticleState.SMOKE;
            this.baseSize *= 1.2f;
            this.friction = SMOKE_FRICTION;
        }

        if (this.currentState == ParticleState.SMOKE) {
            this.dy += this.smokeLift;
        }

        final float percent = (float) this.age / (float) this.lifetime;
        if (this.currentState == ParticleState.PLUME) {
            this.quadSize = this.baseSize + (float) Math.pow(percent, 0.8f) * 2.0f;
        } else {
            this.quadSize = this.baseSize - percent * 2.0f + 2.5f;
        }

        final int presmokeAge = this.smokeTransitionAge - 5;
        if (this.age >= presmokeAge && !this.hasCollided) {
            final float smokePercent = (this.age - presmokeAge) / (float) (this.lifetime - presmokeAge);
            final float agedSpreadMagnitude = (0.8f - smokePercent) * this.spreadMagnitude;
            this.dx += this.spreadDirection.x * SMOKE_SPREAD_MAGNITUDE * agedSpreadMagnitude;
            this.dy += this.spreadDirection.y * SMOKE_SPREAD_MAGNITUDE * agedSpreadMagnitude;
            this.dz += this.spreadDirection.z * SMOKE_SPREAD_MAGNITUDE * agedSpreadMagnitude;
        }

        this.dx *= this.currentFriction;
        this.dy *= this.currentFriction;
        this.dz *= this.currentFriction;

        this.pickSprite();
    }

    private void pickSprite() {
        final int frameIndex;
        if (this.currentState == ParticleState.PLUME) {
            final float plumeProgress = (float) this.age / (float) this.smokeTransitionAge;
            frameIndex = Mth.clamp((int) (plumeProgress * PLUME_SPRITE_COUNT), 0, PLUME_SPRITE_COUNT - 1);
        } else {
            final int ageInSmokePhase = this.age - this.smokeTransitionAge;
            final int smokePhaseDuration = this.lifetime - this.smokeTransitionAge;
            final int smokeFrame = smokePhaseDuration <= 0
                    ? 0
                    : (ageInSmokePhase * SMOKE_SPRITE_COUNT) / smokePhaseDuration;
            frameIndex = PLUME_SPRITE_COUNT + Mth.clamp(smokeFrame, 0, SMOKE_SPRITE_COUNT - 1);
        }
        this.setSprite(this.spriteSet.get(frameIndex, PLUME_SPRITE_COUNT + SMOKE_SPRITE_COUNT));
    }

    private float getRandomSpread() {
        return (random.nextFloat() * 2.0f - 1.0f) * PLUME_SPREAD;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(final SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(final @NotNull SimpleParticleType data,
                                       final @NotNull ClientLevel level,
                                       final double x,
                                       final double y,
                                       final double z,
                                       final double dx,
                                       final double dy,
                                       final double dz) {
            return new ThrusterPlumeParticle(level, x, y, z, dx, dy, dz, this.spriteSet);
        }
    }
}