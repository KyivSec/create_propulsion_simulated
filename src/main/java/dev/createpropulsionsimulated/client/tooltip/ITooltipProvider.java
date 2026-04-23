package dev.createpropulsionsimulated.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;

public interface ITooltipProvider {
    void addText(ItemTooltipEvent event, List<Component> tooltipList);

    default Optional<TooltipComponent> getVisual(final ItemTooltipEvent event) {
        return Optional.empty();
    }
}
