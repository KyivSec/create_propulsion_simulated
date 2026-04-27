package dev.createpropulsionsimulated.client;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.client.particle.ThrusterPlasmaParticle;
import dev.createpropulsionsimulated.client.particle.ThrusterPlumeParticle;
import dev.createpropulsionsimulated.client.particle.ThrusterIonParticle;
import dev.createpropulsionsimulated.client.renderer.CreativeThrusterRenderer;
import dev.createpropulsionsimulated.content.tilt_adapter.TiltAdapterRenderer;
import dev.createpropulsionsimulated.client.visual.CreativeThrusterVisual;
import dev.createpropulsionsimulated.content.ponder.CPSPonderPlugin;
import dev.createpropulsionsimulated.content.wing.CopycatWingModel;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import dev.createpropulsionsimulated.registry.CPSFluids;
import dev.createpropulsionsimulated.registry.CPSItems;
import dev.createpropulsionsimulated.registry.CPSParticleTypes;
import com.simibubi.create.foundation.model.ModelSwapper;
import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = CreatePropulsionSimulated.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CPSClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_COPYCAT_MODEL_SWAP = Boolean.getBoolean("cps.debug.copycatModelSwap");

    private CPSClient() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CPSParticleTypes.PLUME.get(), ThrusterPlumeParticle.Factory::new);
        event.registerSpriteSet(CPSParticleTypes.PLASMA.get(), ThrusterPlasmaParticle.Factory::new);
        event.registerSpriteSet(CPSParticleTypes.ION.get(), ThrusterIonParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        CPSPartialModels.init();
        event.registerBlockEntityRenderer(CPSBlockEntities.CREATIVE_THRUSTER.get(), CreativeThrusterRenderer::new);
        event.registerBlockEntityRenderer(CPSBlockEntities.TILT_ADAPTER.get(), TiltAdapterRenderer::new);
    }

    @SubscribeEvent
    public static void onModelBake(final ModelEvent.ModifyBakingResult event) {
        swapWingModels(event.getModels(), CPSBlocks.COPYCAT_WING.get(), CPSItems.COPYCAT_WING.get(), 4);
        swapWingModels(event.getModels(), CPSBlocks.COPYCAT_WING_8.get(), CPSItems.COPYCAT_WING_8.get(), 8);
        swapWingModels(event.getModels(), CPSBlocks.COPYCAT_WING_12.get(), CPSItems.COPYCAT_WING_12.get(), 12);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new CPSPonderPlugin());
            ItemBlockRenderTypes.setRenderLayer(CPSFluids.TURPENTINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CPSFluids.FLOWING_TURPENTINE.get(), RenderType.translucent());
            final ChunkRenderTypeSet wingRenderTypes = ChunkRenderTypeSet.of(
                    RenderType.solid(),
                    RenderType.cutout(),
                    RenderType.cutoutMipped(),
                    RenderType.translucent()
            );
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING.get(), wingRenderTypes);
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING_8.get(), wingRenderTypes);
            ItemBlockRenderTypes.setRenderLayer(CPSBlocks.COPYCAT_WING_12.get(), wingRenderTypes);
            SimpleBlockEntityVisualizer
                    .builder(CPSBlockEntities.CREATIVE_THRUSTER.get())
                    .factory(CreativeThrusterVisual::new)
                    .skipVanillaRender(be -> true)
                    .apply();
            SimpleBlockEntityVisualizer
                    .builder(CPSBlockEntities.TILT_ADAPTER.get())
                    .factory(dev.createpropulsionsimulated.client.visual.TiltAdapterVisual::new)
                    .skipVanillaRender(be -> false)
                    .apply();
        });
    }

    private static void swapWingModels(final Map<ModelResourceLocation, BakedModel> models,
                                       final Block block,
                                       final Item item,
                                       final int width) {
        final Set<ModelResourceLocation> targetLocations = new LinkedHashSet<>();
        final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        targetLocations.add(new ModelResourceLocation(blockId, ""));
        targetLocations.addAll(ModelSwapper.getAllBlockStateModelLocations(block));
        targetLocations.add(ModelSwapper.getItemModelLocation(item));

        int presentLocations = 0;
        int wrappedLocations = 0;
        for (final ModelResourceLocation location : targetLocations) {
            final BakedModel original = models.get(location);
            if (original == null) {
                continue;
            }
            presentLocations++;
            models.put(location, CopycatWingModel.create(width).apply(original));
            wrappedLocations++;
        }

        if (DEBUG_COPYCAT_MODEL_SWAP) {
            LOGGER.info(
                    "[CPS] Copycat wing model swap width={} block={} totalTargets={} present={} wrapped={}",
                    width,
                    blockId,
                    targetLocations.size(),
                    presentLocations,
                    wrappedLocations
            );
        }
    }
}
