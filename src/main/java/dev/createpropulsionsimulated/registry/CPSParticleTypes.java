package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, CreatePropulsionSimulated.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLUME = REGISTER.register("plume",
            () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA = REGISTER.register("plasma",
            () -> new SimpleParticleType(false));

    private CPSParticleTypes() {
    }
}
