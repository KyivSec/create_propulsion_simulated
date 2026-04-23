package dev.createpropulsionsimulated.client.tooltip;

import dev.createpropulsionsimulated.content.thruster.FluidThrusterProperties;
import dev.createpropulsionsimulated.registry.CPSFluids;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public final class FuelTooltipProvider implements ITooltipProvider {
    @Override
    public void addText(final ItemTooltipEvent event, final List<Component> tooltipList) {
        final ItemStack stack = event.getItemStack();
        final var fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return;
        }

        final FluidStack fluidStack = fluidHandler.getFluidInTank(0);
        if (fluidStack.isEmpty() || !fluidStack.getFluid().isSame(CPSFluids.TURPENTINE.get())) {
            return;
        }

        final FluidThrusterProperties properties = FluidThrusterProperties.TURPENTINE;
        TooltipHandler.wrapShiftHoldText(tooltipList, "createpropulsionsimulated.tooltip.holdForRocketFuelSummary", () -> {
            final int thrustPercent = Math.round(properties.thrustMultiplier() * 100.0f);
            final Component thrustLine = Component.translatable("createpropulsionsimulated.tooltip.thrust")
                    .append(": ")
                    .withStyle(Palette.STANDARD_CREATE.primary())
                    .append(Component.literal(Integer.toString(thrustPercent)).withStyle(Palette.STANDARD_CREATE.highlight()))
                    .append(Component.literal("%").withStyle(Palette.STANDARD_CREATE.primary()));
            tooltipList.add(thrustLine);

            final int consumptionPercent = Math.round(properties.consumptionMultiplier() * 100.0f);
            final Component consumptionLine = Component.translatable("createpropulsionsimulated.tooltip.consumption")
                    .append(": ")
                    .withStyle(Palette.STANDARD_CREATE.primary())
                    .append(Component.literal(Integer.toString(consumptionPercent)).withStyle(Palette.STANDARD_CREATE.highlight()))
                    .append(Component.literal("%").withStyle(Palette.STANDARD_CREATE.primary()));
            tooltipList.add(consumptionLine);
            tooltipList.add(Component.empty());
        });
    }

    @Override
    public Optional<TooltipComponent> getVisual(final ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        final var fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return Optional.empty();
        }

        final FluidStack fluidStack = fluidHandler.getFluidInTank(0);
        if (fluidStack.isEmpty() || !fluidStack.getFluid().isSame(CPSFluids.TURPENTINE.get())) {
            return Optional.empty();
        }

        final int capacity = fluidHandler.getTankCapacity(0);
        if (capacity <= 0) {
            return Optional.empty();
        }

        return Optional.of(new FluidBarVisualData(fluidStack.copy(), fluidStack.getAmount(), capacity));
    }
}
