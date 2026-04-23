package dev.createpropulsionsimulated.content.thruster;

import com.mojang.serialization.MapCodec;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

public final class CreativeThrusterBlock extends AbstractThrusterBlock {
    public static final MapCodec<CreativeThrusterBlock> CODEC = simpleCodec(CreativeThrusterBlock::new);
    public static final DirectionProperty PLACEMENT_FACING = DirectionProperty.create("placement_facing", Direction.values());

    public CreativeThrusterBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(PLACEMENT_FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PLACEMENT_FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return state.setValue(PLACEMENT_FACING, context.getClickedFace().getOpposite());
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
        return ThrusterShapes.CREATIVE_THRUSTER.get(direction);
    }

    @Override
    public Class<ThrusterBlockEntity> getBlockEntityClass() {
        return ThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrusterBlockEntity> getBlockEntityType() {
        return CPSBlockEntities.CREATIVE_THRUSTER.get();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isCreativeVariant() {
        return true;
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            this.withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
                be.cyclePlumeType();
                IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            });
        }
        return InteractionResult.SUCCESS;
    }
}
