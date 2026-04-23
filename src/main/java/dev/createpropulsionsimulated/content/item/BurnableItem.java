package dev.createpropulsionsimulated.content.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class BurnableItem extends Item {
    private final int burnTime;

    public BurnableItem(final Properties properties, final int burnTime) {
        super(properties);
        this.burnTime = burnTime;
    }

    @Override
    public int getBurnTime(final ItemStack stack, final RecipeType<?> recipeType) {
        return this.burnTime;
    }
}
