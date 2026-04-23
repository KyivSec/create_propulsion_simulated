package dev.createpropulsionsimulated.client;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class CPSPartialModels {
    public static final PartialModel CREATIVE_THRUSTER_BRACKET = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/creative_thruster_bracket"));

    private CPSPartialModels() {
    }

    public static void init() {
    }
}
