package dev.createpropulsionsimulated.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidBarVisualData implements TooltipComponent {
    private final FluidStack fluidStack;
    private final int amount;
    private final int capacity;

    public FluidBarVisualData(final FluidStack fluidStack, final int amount, final int capacity) {
        this.fluidStack = fluidStack.copy();
        this.amount = amount;
        this.capacity = capacity;
    }

    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public int getAmount() {
        return this.amount;
    }

    public int getCapacity() {
        return this.capacity;
    }
}
