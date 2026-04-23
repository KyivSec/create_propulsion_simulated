package dev.createpropulsionsimulated.client;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.client.particle.ThrusterPlasmaParticle;
import dev.createpropulsionsimulated.client.particle.ThrusterPlumeParticle;
import dev.createpropulsionsimulated.client.renderer.CreativeThrusterRenderer;
import dev.createpropulsionsimulated.client.visual.CreativeThrusterVisual;
import dev.createpropulsionsimulated.content.ponder.CPSPonderPlugin;
import dev.createpropulsionsimulated.content.wing.CopycatWingModel;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import dev.createpropulsionsimulated.registry.CPSFluids;
import dev.createpropulsionsimulated.registry.CPSItems;
import dev.createpropulsionsimulated.registry.CPSParticleTypes;
import com.simibubi.create.foundation.model.ModelSwapper;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = CreatePropulsionSimulated.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CPSClient {
    private CPSClient() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CPSParticleTypes.PLUME.get(), ThrusterPlumeParticle.Factory::new);
        event.registerSpriteSet(CPSParticleTypes.PLASMA.get(), ThrusterPlasmaParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        CPSPartialModels.init();
        event.registerBlockEntityRenderer(CPSBlockEntities.CREATIVE_THRUSTER.get(), CreativeThrusterRenderer::new);
    }

    @SubscribeEvent
    public static void onModelBake(final ModelEvent.ModifyBakingResult event) {
        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getAllBlockStateModelLocations(CPSBlocks.COPYCAT_WING.get()), CopycatWingModel.create(4));
        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getAllBlockStateModelLocations(CPSBlocks.COPYCAT_WING_8.get()), CopycatWingModel.create(8));
        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getAllBlockStateModelLocations(CPSBlocks.COPYCAT_WING_12.get()), CopycatWingModel.create(12));

        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getItemModelLocation(CPSItems.COPYCAT_WING.get()), CopycatWingModel.create(4));
        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getItemModelLocation(CPSItems.COPYCAT_WING_8.get()), CopycatWingModel.create(8));
        ModelSwapper.swapModels(event.getModels(), ModelSwapper.getItemModelLocation(CPSItems.COPYCAT_WING_12.get()), CopycatWingModel.create(12));
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new CPSPonderPlugin());
            ItemBlockRenderTypes.setRenderLayer(CPSFluids.TURPENTINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CPSFluids.FLOWING_TURPENTINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING_8.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING_12.get(), RenderType.cutoutMipped());
            SimpleBlockEntityVisualizer
                    .builder(CPSBlockEntities.CREATIVE_THRUSTER.get())
                    .factory(CreativeThrusterVisual::new)
                    .skipVanillaRender(be -> true)
                    .apply();
        });
    }
}
