package dev.createpropulsionsimulated.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ThrusterIonParticle extends SimpleAnimatedParticle {
    private static final float BASE_QUAD_SIZE = 0.95f;
    private static final float END_QUAD_SIZE = 0.2f;
    private static final float SPEED_MULTIPLIER = 0.144f;
    private static final float FRICTION = 0.995f;
    private static final int BASE_LIFETIME = 20;
    private static final double FLUCTUATION = 0.0025d;

    private static final int SPRITE_COUNT = 9;
    private final SpriteSet spriteSet;
    private final float startSize;

    protected ThrusterIonParticle(final ClientLevel level,
                                  final double x,
                                  final double y,
                                  final double z,
                                  final double dx,
                                  final double dy,
                                  final double dz,
                                  final SpriteSet spriteSet) {
        super(level, x, y, z, spriteSet, 0);
        this.spriteSet = spriteSet;
        this.hasPhysics = false;
        this.friction = FRICTION;
        this.lifetime = BASE_LIFETIME;

        this.xd = dx * SPEED_MULTIPLIER;
        this.yd = dy * SPEED_MULTIPLIER;
        this.zd = dz * SPEED_MULTIPLIER;

        this.quadSize = BASE_QUAD_SIZE;
        this.startSize = this.quadSize;
        this.setColor(0xFFFFFF);
        this.setAlpha(1.0f);
        this.pickSpriteAndSize();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.yd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.zd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        this.pickSpriteAndSize();
    }

    private void pickSpriteAndSize() {
        final float progress = Mth.clamp((float) this.age / (float) this.lifetime, 0.0f, 1.0f);
        final int frameIndex = Mth.clamp((int) (progress * SPRITE_COUNT), 0, SPRITE_COUNT - 1);
        this.setSprite(this.spriteSet.get(frameIndex, SPRITE_COUNT));
        this.quadSize = Mth.lerp(progress, this.startSize, END_QUAD_SIZE);
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
            return new ThrusterIonParticle(level, x, y, z, dx, dy, dz, this.spriteSet);
        }
    }
}
