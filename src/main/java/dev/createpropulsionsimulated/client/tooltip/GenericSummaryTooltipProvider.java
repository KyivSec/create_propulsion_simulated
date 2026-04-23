package dev.createpropulsionsimulated.client.tooltip;

import com.simibubi.create.foundation.item.TooltipHelper;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public final class GenericSummaryTooltipProvider implements ITooltipProvider {
    @Override
    public void addText(final ItemTooltipEvent event, final List<Component> tooltipList) {
        final Item item = event.getItemStack().getItem();
        final ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !"createpropulsionsimulated".equals(id.getNamespace())) {
            return;
        }

        final String path = "createpropulsionsimulated." + id.getPath();
        final String summaryKey = path + ".tooltip.summary";
        if (!I18n.exists(summaryKey)) {
            return;
        }

        TooltipHandler.wrapShiftHoldText(tooltipList, "create.tooltip.holdForDescription", () -> {
            tooltipList.addAll(TooltipHelper.cutStringTextComponent(
                    Component.translatable(summaryKey).getString(),
                    Palette.STANDARD_CREATE
            ));
        });
    }
}
