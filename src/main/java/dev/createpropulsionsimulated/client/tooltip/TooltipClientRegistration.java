package dev.createpropulsionsimulated.client.tooltip;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(modid = CreatePropulsionSimulated.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TooltipClientRegistration {
    private TooltipClientRegistration() {
    }

    @SubscribeEvent
    public static void registerTooltipComponentFactories(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(FluidBarVisualData.class, FluidBarVisualRenderer::new);
    }
}
