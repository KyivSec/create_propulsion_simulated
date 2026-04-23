package dev.createpropulsionsimulated.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.createpropulsionsimulated.client.CPSPartialModels;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlock;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlockEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class CreativeThrusterRenderer extends SmartBlockEntityRenderer<CreativeThrusterBlockEntity> {
    public CreativeThrusterRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final CreativeThrusterBlockEntity be,
                              final float partialTicks,
                              final PoseStack ms,
                              final MultiBufferSource buffer,
                              final int light,
                              final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }

        final BlockState state = be.getBlockState();
        if (!state.hasProperty(CreativeThrusterBlock.PLACEMENT_FACING)) {
            return;
        }

        final Direction facing = state.getValue(CreativeThrusterBlock.FACING);
        final Direction placementFacing = state.getValue(CreativeThrusterBlock.PLACEMENT_FACING);
        if (facing.getAxis() == placementFacing.getAxis()) {
            return;
        }

        final SuperByteBuffer bracket = CachedBuffers.partial(CPSPartialModels.CREATIVE_THRUSTER_BRACKET, state);
        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        final float angle = this.getBracketAngle(facing, placementFacing);

        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        ms.mulPose(Axis.XP.rotationDegrees(90));
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.translate(-0.5, -0.5, -0.5);

        bracket.light(light).overlay(overlay).renderInto(ms, vb);
        ms.popPose();
    }

    private float getBracketAngle(final Direction facing, final Direction placementFacing) {
        final Vector3f local = new Vector3f(placementFacing.step());
        local.rotate(facing.getRotation().conjugate());
        local.rotateX((float) Math.toRadians(-90));
        final double targetAngle = Math.toDegrees(Math.atan2(local.y, local.x));
        return (float) (targetAngle + 90);
    }
}
