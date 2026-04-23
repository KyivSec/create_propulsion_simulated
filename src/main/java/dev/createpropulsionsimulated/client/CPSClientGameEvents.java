package dev.createpropulsionsimulated.client;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.client.tooltip.TooltipHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CreatePropulsionSimulated.MOD_ID, value = Dist.CLIENT)
public final class CPSClientGameEvents {
    private CPSClientGameEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event) {
        TooltipHandler.addToItemTooltip(event);
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(final RenderTooltipEvent.GatherComponents event) {
        TooltipHandler.addToTooltipComponents(event);
    }
}
