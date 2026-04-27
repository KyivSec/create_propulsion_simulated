package dev.createpropulsionsimulated.client;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class CPSPartialModels {
    public static final PartialModel CREATIVE_THRUSTER_BRACKET = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/creative_thruster_bracket"));

    public static final PartialModel TILT_ADAPTER_INPUT_SHAFT = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/tilt_adapter_input_shaft"));
    public static final PartialModel TILT_ADAPTER_OUTPUT_SHAFT = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/tilt_adapter_output_shaft"));
    public static final PartialModel TILT_ADAPTER_GANTRY = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/tilt_adapter_gantry"));
    public static final PartialModel TILT_ADAPTER_SIDE_INDICATOR = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(CreatePropulsionSimulated.MOD_ID, "partial/tilt_adapter_side_overlay"));

    private CPSPartialModels() {
    }

    public static void init() {
    }
}
