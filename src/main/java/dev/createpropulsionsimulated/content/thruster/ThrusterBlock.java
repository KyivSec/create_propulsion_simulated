package dev.createpropulsionsimulated.content.thruster;

import com.mojang.serialization.MapCodec;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ThrusterBlock extends AbstractThrusterBlock {
    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    public ThrusterBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(final BlockState state,
                                  final BlockGetter level,
                                  final BlockPos pos,
                                  final CollisionContext context) {
        Direction direction = state.getValue(FACING);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            direction = direction.getOpposite();
        }
        return ThrusterShapes.THRUSTER.get(direction);
    }

    @Override
    public Class<ThrusterBlockEntity> getBlockEntityClass() {
        return ThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrusterBlockEntity> getBlockEntityType() {
        return CPSBlockEntities.THRUSTER.get();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
