package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(Registries.SOUND_EVENT, CreatePropulsionSimulated.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> THRUSTER_LOOP = REGISTER.register(
            "thruster_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "thruster_loop"))
    );

    private CPSSounds() {
    }
}
