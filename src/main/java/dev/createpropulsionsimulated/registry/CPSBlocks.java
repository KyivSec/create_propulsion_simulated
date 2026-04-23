package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlock;
import dev.createpropulsionsimulated.content.thruster.IonThrusterBlock;
import dev.createpropulsionsimulated.content.thruster.ThrusterBlock;
import dev.createpropulsionsimulated.content.wing.CopycatWingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(CreatePropulsionSimulated.MOD_ID);

    public static final DeferredBlock<ThrusterBlock> THRUSTER = REGISTER.register("thruster",
            () -> new ThrusterBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<CreativeThrusterBlock> CREATIVE_THRUSTER = REGISTER.register("creative_thruster",
            () -> new CreativeThrusterBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<IonThrusterBlock> ION_THRUSTER = REGISTER.register("ion_thruster",
            () -> new IonThrusterBlock(BlockBehaviour.Properties.of()
                    .strength(3.5f, 6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING = REGISTER.register("copycat_wing",
            () -> new CopycatWingBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 2.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion(), 4, () -> CPSItems.COPYCAT_WING.get()));

    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING_8 = REGISTER.register("copycat_wing_8",
            () -> new CopycatWingBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 2.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion(), 8, () -> CPSItems.COPYCAT_WING.get()));

    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING_12 = REGISTER.register("copycat_wing_12",
            () -> new CopycatWingBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 2.0f)
                    .sound(SoundType.COPPER)
                    .noOcclusion(), 12, () -> CPSItems.COPYCAT_WING.get()));

    private CPSBlocks() {
    }
}
