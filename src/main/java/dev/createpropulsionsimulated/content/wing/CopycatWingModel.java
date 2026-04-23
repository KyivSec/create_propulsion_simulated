package dev.createpropulsionsimulated.content.wing;

import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CopycatWingModel extends CopycatModel {
    private static final float PIXELS_PER_BLOCK = 16.0f;
    private final int width;

    public CopycatWingModel(final BakedModel originalModel, final int width) {
        super(originalModel);
        this.width = width;
    }

    public static Function<BakedModel, ? extends BakedModel> create(final int width) {
        return bakedModel -> new CopycatWingModel(bakedModel, width);
    }

    @Override
    protected List<BakedQuad> getCroppedQuads(final BlockState state,
                                              final Direction side,
                                              final RandomSource random,
                                              final BlockState material,
                                              final ModelData wrappedData,
                                              final RenderType renderType) {
        Direction facing = state.getValue(CopycatWingBlock.FACING);
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            facing = facing.getOpposite();
        }

        Direction.Axis axis = facing.getAxis();
        BakedModel model = getModelOf(material);
        List<BakedQuad> templateQuads = model.getQuads(material, side, random, wrappedData, renderType);
        List<BakedQuad> quads = new ArrayList<>();

        float halfWidth = width / 2f;
        float offsetDistance = (PIXELS_PER_BLOCK - width) / (2 * PIXELS_PER_BLOCK);

        for (boolean positiveSide : Iterate.trueAndFalse) {
            Direction culledFace = positiveSide ? facing.getOpposite() : facing;
            Vec3i placementNormal = culledFace.getNormal();
            Vec3 placementOffset = new Vec3(
                    placementNormal.getX() * offsetDistance,
                    placementNormal.getY() * offsetDistance,
                    placementNormal.getZ() * offsetDistance
            );

            float cropExtent = halfWidth / PIXELS_PER_BLOCK;
            float minX = 0;
            float minY = 0;
            float minZ = 0;
            float maxX = 1;
            float maxY = 1;
            float maxZ = 1;

            switch (axis) {
                case X -> {
                    if (!positiveSide) {
                        minX = 1 - cropExtent;
                    } else {
                        maxX = cropExtent;
                    }
                }
                case Y -> {
                    if (!positiveSide) {
                        minY = 1 - cropExtent;
                    } else {
                        maxY = cropExtent;
                    }
                }
                case Z -> {
                    if (!positiveSide) {
                        minZ = 1 - cropExtent;
                    } else {
                        maxZ = cropExtent;
                    }
                }
            }

            AABB croppingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            for (BakedQuad quad : templateQuads) {
                if (quad.getDirection() == culledFace) {
                    continue;
                }
                quads.add(BakedQuadHelper.cloneWithCustomGeometry(
                        quad,
                        BakedModelHelper.cropAndMove(quad.getVertices(), quad.getSprite(), croppingBox, placementOffset)
                ));
            }
        }

        return quads;
    }
}
