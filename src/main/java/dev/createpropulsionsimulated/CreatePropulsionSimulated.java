package dev.createpropulsionsimulated;

import dev.createpropulsionsimulated.config.ThrusterConfig;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import dev.createpropulsionsimulated.registry.CPSCaps;
import dev.createpropulsionsimulated.registry.CPSCreativeTabs;
import dev.createpropulsionsimulated.registry.CPSFluids;
import dev.createpropulsionsimulated.registry.CPSItems;
import dev.createpropulsionsimulated.registry.CPSParticleTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(CreatePropulsionSimulated.MOD_ID)
public final class CreatePropulsionSimulated {
    public static final String MOD_ID = "createpropulsionsimulated";

    public CreatePropulsionSimulated(final IEventBus modBus, final ModContainer modContainer) {
        CPSFluids.FLUID_TYPES.register(modBus);
        CPSFluids.FLUIDS.register(modBus);
        CPSFluids.BLOCKS.register(modBus);

        CPSBlocks.REGISTER.register(modBus);
        CPSItems.REGISTER.register(modBus);
        CPSBlockEntities.REGISTER.register(modBus);
        CPSCreativeTabs.REGISTER.register(modBus);
        CPSParticleTypes.REGISTER.register(modBus);

        modBus.addListener(this::onRegisterCapabilities);
        modContainer.registerConfig(ModConfig.Type.SERVER, ThrusterConfig.SPEC);
    }

    private void onRegisterCapabilities(final RegisterCapabilitiesEvent event) {
        CPSCaps.register(event);
    }
}
