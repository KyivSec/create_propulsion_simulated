package dev.createpropulsionsimulated.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public final class TooltipHandler {
    private static final List<ITooltipProvider> TOP_PROVIDERS = new ArrayList<>();

    static {
        TOP_PROVIDERS.add(new GenericSummaryTooltipProvider());
        TOP_PROVIDERS.add(new FuelTooltipProvider());
    }

    private TooltipHandler() {
    }

    public static void addToItemTooltip(final ItemTooltipEvent event) {
        if (event.getItemStack().isEmpty()) {
            return;
        }

        final List<Component> currentTooltip = event.getToolTip();
        final List<Component> topList = new ArrayList<>();
        for (final ITooltipProvider provider : TOP_PROVIDERS) {
            provider.addText(event, topList);
        }
        if (topList.isEmpty()) {
            return;
        }

        final int insertIndex = currentTooltip.isEmpty() ? 0 : 1;
        currentTooltip.addAll(insertIndex, topList);
    }

    public static void addToTooltipComponents(final RenderTooltipEvent.GatherComponents event) {
        final ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        final ItemTooltipEvent syntheticEvent = new ItemTooltipEvent(
                stack,
                Minecraft.getInstance().player,
                new ArrayList<>(),
                TooltipFlag.Default.NORMAL,
                Item.TooltipContext.EMPTY
        );

        final List<TooltipComponent> visualComponents = new ArrayList<>();
        for (final ITooltipProvider provider : TOP_PROVIDERS) {
            provider.getVisual(syntheticEvent).ifPresent(visualComponents::add);
        }
        if (visualComponents.isEmpty()) {
            return;
        }

        final List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        int insertIndex = elements.isEmpty() ? 0 : 1;
        for (final TooltipComponent component : visualComponents) {
            elements.add(insertIndex++, Either.right(component));
        }
    }

    public static void wrapShiftHoldText(final List<Component> tooltipList, final String langKey, final Runnable addDetailedContent) {
        final boolean isShiftDown = Screen.hasShiftDown();
        final Component keyComponent = Component.translatable("create.tooltip.keyShift")
                .withStyle(isShiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY);
        tooltipList.add(Component.translatable(langKey, keyComponent).withStyle(ChatFormatting.DARK_GRAY));

        if (isShiftDown) {
            tooltipList.add(Component.empty());
            addDetailedContent.run();
        }
    }
}
