package dev.createpropulsionsimulated.content.wing;

import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import dev.createpropulsionsimulated.registry.CPSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatWingItem extends BlockItem {
    public CopycatWingItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState clickedState = world.getBlockState(pos);

        if (player != null && player.isShiftKeyDown() && clickedState.getBlock() instanceof CopycatWingBlock clickedWing) {
            if (clickedWing.getWidth() != 12) {
                BlockState targetState = (clickedWing.getWidth() == 4)
                        ? CPSBlocks.COPYCAT_WING_8.get().defaultBlockState()
                        : CPSBlocks.COPYCAT_WING_12.get().defaultBlockState();
                targetState = targetState.setValue(CopycatWingBlock.FACING, clickedState.getValue(CopycatWingBlock.FACING));

                if (!world.isClientSide()) {
                    CompoundTag oldBlockEntityData = null;
                    BlockEntity oldBE = world.getBlockEntity(pos);
                    if (oldBE instanceof CopycatBlockEntity) {
                        oldBlockEntityData = oldBE.saveWithFullMetadata(world.registryAccess());
                    }

                    world.setBlock(pos, targetState, 3);

                    if (oldBlockEntityData != null) {
                        BlockEntity newBE = world.getBlockEntity(pos);
                        if (newBE instanceof CopycatBlockEntity) {
                            newBE.loadWithComponents(oldBlockEntityData, world.registryAccess());
                            newBE.setChanged();
                        }
                    }

                    if (!player.getAbilities().instabuild) {
                        context.getItemInHand().shrink(1);
                    }
                }

                world.playSound(player, pos, targetState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(world.isClientSide());
            }
        }

        return super.useOn(context);
    }
}
