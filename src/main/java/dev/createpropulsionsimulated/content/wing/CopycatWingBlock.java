package dev.createpropulsionsimulated.content.wing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.createpropulsionsimulated.content.thruster.ThrusterShapes;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import dev.createpropulsionsimulated.registry.CPSItems;
import net.createmod.catnip.math.VoxelShaper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CopycatWingBlock extends CopycatBlock implements BlockSubLevelLiftProvider {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final List<Supplier<? extends Block>> PLACEMENT_BLOCKS = List.of(
            CPSBlocks.COPYCAT_WING,
            CPSBlocks.COPYCAT_WING_8,
            CPSBlocks.COPYCAT_WING_12
    );
    private static final int PLACEMENT_HELPER_ID = PlacementHelpers.register(new WingPlacementHelper(PLACEMENT_BLOCKS));
    private static final Map<Integer, VoxelShaper> WING_SHAPERS = Map.of(
            4, wingShape(4),
            8, wingShape(8),
            12, wingShape(12)
    );

    private final int width;
    private final Supplier<Item> baseItemSupplier;

    public CopycatWingBlock(final Properties properties, final int width, final Supplier<Item> baseItemSupplier) {
        super(properties);
        this.width = width;
        this.baseItemSupplier = baseItemSupplier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public int getWidth() {
        return width;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter level,
                               final BlockPos pos,
                               final CollisionContext context) {
        return WING_SHAPERS.get(width).get(state.getValue(FACING));
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack heldItem,
                                              final BlockState state,
                                              final Level world,
                                              final BlockPos pos,
                                              final Player player,
                                              final InteractionHand hand,
                                              final BlockHitResult ray) {
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            IPlacementHelper placementHelper = PlacementHelpers.get(PLACEMENT_HELPER_ID);
            if (placementHelper.matchesItem(heldItem)) {
                placementHelper.getOffset(player, world, state, pos, ray)
                        .placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(heldItem, state, world, pos, player, hand, ray);
    }

    @Override
    public ItemStack getCloneItemStack(final LevelReader level,
                                       final BlockPos pos,
                                       final BlockState state) {
        BlockState material = getMaterial(level, pos);
        if (AllBlocks.COPYCAT_BASE.has(material)) {
            return new ItemStack(baseItemSupplier.get());
        }
        return material.getBlock().getCloneItemStack(level, pos, material);
    }

    @Override
    public boolean canConnectTexturesToward(final BlockAndTintGetter reader,
                                            final BlockPos fromPos,
                                            final BlockPos toPos,
                                            final BlockState state) {
        BlockState toState = reader.getBlockState(toPos);
        if (!toState.is(this)) {
            return false;
        }
        return state.getValue(FACING) == toState.getValue(FACING);
    }

    @Override
    public boolean isIgnoredConnectivitySide(final BlockAndTintGetter reader,
                                             final BlockState state,
                                             final Direction face,
                                             final BlockPos fromPos,
                                             final BlockPos toPos) {
        if (fromPos == null || toPos == null) {
            return true;
        }

        BlockState toState = reader.getBlockState(toPos);
        Direction facing = state.getValue(FACING);
        if (!toState.is(this)) {
            return facing != face.getOpposite();
        }

        Direction toFacing = toState.getValue(FACING);
        BlockPos diff = toPos.subtract(fromPos);
        if (diff.getX() == 0 && diff.getZ() == 0 && diff.getY() != 0
                && facing.getAxis() == Direction.Axis.Y && toFacing.getAxis() == Direction.Axis.Y) {
            return true;
        }
        if (diff.getY() == 0 && diff.getZ() == 0 && diff.getX() != 0
                && facing.getAxis() == Direction.Axis.X && toFacing.getAxis() == Direction.Axis.X) {
            return true;
        }
        if (diff.getX() == 0 && diff.getY() == 0 && diff.getZ() != 0
                && facing.getAxis() == Direction.Axis.Z && toFacing.getAxis() == Direction.Axis.Z) {
            return true;
        }
        return false;
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootParams.Builder builder) {
        int dropCount = width / 4;
        return List.of(new ItemStack(CPSItems.COPYCAT_WING.get(), dropCount));
    }

    @Override
    public void onRemove(final BlockState state,
                         final Level level,
                         final BlockPos pos,
                         final BlockState newState,
                         final boolean isMoving) {
        if (newState.getBlock() instanceof CopycatWingBlock) {
            if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
                level.removeBlockEntity(pos);
            }
            return;
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<CopycatBlockEntity> getBlockEntityClass() {
        return CopycatBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CopycatBlockEntity> getBlockEntityType() {
        return CPSBlockEntities.COPYCAT_WING.get();
    }

    @Override
    public float sable$getLiftScalar() {
        float liftRatio = 1.0f;
        if (width == 8) liftRatio = 170.0f / 150.0f;
        if (width == 12) liftRatio = 190.0f / 150.0f;
        return 0.475f * liftRatio;
    }

    @Override
    public float sable$getParallelDragScalar() {
        float dragRatio = 1.0f;
        if (width == 8) dragRatio = 160.0f / 150.0f;
        if (width == 12) dragRatio = 170.0f / 150.0f;
        return 1.75f * dragRatio;
    }

    @Override
    public float sable$getDirectionlessDragScalar() {
        float k1 = sable$getParallelDragScalar();
        float k2 = sable$getLiftScalar();
        return (float) ((-k1 + Math.sqrt(k1 * k1 + k2 * k2)) / 2.0);
    }

    @Override
    public @NotNull Direction sable$getNormal(final BlockState blockState) {
        Direction facing = blockState.getValue(FACING);
        return Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis());
    }

    private static VoxelShaper wingShape(final int width) {
        int halfWidth = width / 2;
        return ThrusterShapes.ShapeBuilder.shape()
                .add(Block.box(0, 8 - halfWidth, 0, 16, 8 + halfWidth, 16))
                .forDirectional(Direction.UP);
    }

    @Override
    public void fallOn(final Level level, final BlockState state, final BlockPos pos, final Entity entity, final float fallDistance) {
        super.fallOn(level, state, pos, entity, 0);
    }

    @Override
    public void updateEntityAfterFallOn(final BlockGetter level, final Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            this.bounce(entity);
        }
    }

    private void bounce(final Entity pEntity) {
        final Vec3 Vec3 = pEntity.getDeltaMovement();
        if (Vec3.y < 0.0D) {
            final double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
            pEntity.setDeltaMovement(Vec3.x, -Vec3.y * (double) 0.26F * d0, Vec3.z);
        }
    }
}
