package dev.createpropulsionsimulated.content.wing;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WingPlacementHelper implements IPlacementHelper {
    private final List<Supplier<? extends Block>> blocks;

    public WingPlacementHelper(final List<Supplier<? extends Block>> blocks) {
        this.blocks = blocks;
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return stack -> blocks.stream().anyMatch(block -> block.get().asItem() == stack.getItem());
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return state -> blocks.stream().anyMatch(block -> state.is(block.get()));
    }

    @Override
    public PlacementOffset getOffset(final Player player,
                                     final Level world,
                                     final BlockState state,
                                     final BlockPos pos,
                                     final BlockHitResult ray) {
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getLocation(),
                state.getValue(BlockStateProperties.FACING).getAxis(),
                dir -> world.getBlockState(pos.relative(dir)).canBeReplaced()
        );

        if (directions.isEmpty()) {
            return PlacementOffset.fail();
        }

        return PlacementOffset.success(pos.relative(directions.get(0)),
                blockState -> blockState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING)));
    }
}
