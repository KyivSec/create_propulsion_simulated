package dev.createpropulsionsimulated.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidBarVisualRenderer implements ClientTooltipComponent {
    private static final int OUTER_WIDTH = 60;
    private static final int OUTER_HEIGHT = 10;
    private static final int INNER_X_OFFSET = 2;
    private static final int INNER_Y_OFFSET = 2;
    private static final int INNER_WIDTH = OUTER_WIDTH - (INNER_X_OFFSET * 2);
    private static final int INNER_HEIGHT = OUTER_HEIGHT - (INNER_Y_OFFSET * 2);
    private static final int TILE_SIZE = 16;

    private final FluidBarVisualData data;

    public FluidBarVisualRenderer(final FluidBarVisualData data) {
        this.data = data;
    }

    @Override
    public int getWidth(final Font font) {
        return OUTER_WIDTH;
    }

    @Override
    public int getHeight() {
        return OUTER_HEIGHT + 2;
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics guiGraphics) {
        guiGraphics.fill(x, y, x + OUTER_WIDTH, y + OUTER_HEIGHT, 0xFF121212);
        guiGraphics.fill(x + 1, y + 1, x + OUTER_WIDTH - 1, y + OUTER_HEIGHT - 1, 0xFF2A2A2A);
        guiGraphics.fill(x + INNER_X_OFFSET, y + INNER_Y_OFFSET, x + INNER_X_OFFSET + INNER_WIDTH, y + INNER_Y_OFFSET + INNER_HEIGHT, 0xFF101010);

        final FluidStack fluidStack = this.data.getFluidStack();
        final int amount = this.data.getAmount();
        final int capacity = this.data.getCapacity();
        if (fluidStack.isEmpty() || amount <= 0 || capacity <= 0) {
            return;
        }

        final int filledWidth = Math.clamp(Math.round((amount / (float) capacity) * INNER_WIDTH), 0, INNER_WIDTH);
        if (filledWidth <= 0) {
            return;
        }

        final IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        final ResourceLocation stillTexture = extensions.getStillTexture(fluidStack);
        if (stillTexture == null) {
            return;
        }

        final TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);

        final int tintColor = extensions.getTintColor(fluidStack);
        float alpha = ((tintColor >> 24) & 0xFF) / 255.0f;
        if (alpha <= 0.0f) {
            alpha = 1.0f;
        }
        final float red = ((tintColor >> 16) & 0xFF) / 255.0f;
        final float green = ((tintColor >> 8) & 0xFF) / 255.0f;
        final float blue = (tintColor & 0xFF) / 255.0f;

        guiGraphics.setColor(red, green, blue, alpha);
        int rendered = 0;
        while (rendered < filledWidth) {
            final int tileWidth = Math.min(TILE_SIZE, filledWidth - rendered);
            final float uMax = tileWidth / (float) TILE_SIZE;
            guiGraphics.blit(
                    x + INNER_X_OFFSET + rendered,
                    y + INNER_Y_OFFSET,
                    0,
                    tileWidth,
                    INNER_HEIGHT,
                    sprite,
                    0.0f,
                    0.0f,
                    uMax,
                    1.0f
            );
            rendered += tileWidth;
        }

        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
