package dev.createpropulsionsimulated.content.wing;

import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatWingBlockEntity extends CopycatBlockEntity {
    public CopycatWingBlockEntity(final BlockPos pos, final BlockState state) {
        super(CPSBlockEntities.COPYCAT_WING.get(), pos, state);
    }
}
