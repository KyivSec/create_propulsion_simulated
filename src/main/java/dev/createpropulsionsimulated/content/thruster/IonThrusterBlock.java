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

public class IonThrusterBlock extends AbstractThrusterBlock {
    public static final MapCodec<IonThrusterBlock> CODEC = simpleCodec(IonThrusterBlock::new);

    public IonThrusterBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(final BlockState state,
                                  final BlockGetter level,
                                  final BlockPos pos,
                                  final CollisionContext context) {
        final Direction direction = state.getValue(FACING);
        return ThrusterShapes.ION_THRUSTER.get(direction);
    }

    @Override
    public Class<ThrusterBlockEntity> getBlockEntityClass() {
        return ThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrusterBlockEntity> getBlockEntityType() {
        return CPSBlockEntities.ION_THRUSTER.get();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
