package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlockEntity;
import dev.createpropulsionsimulated.content.thruster.IonThrusterBlockEntity;
import dev.createpropulsionsimulated.content.thruster.ThrusterBlockEntity;
import dev.createpropulsionsimulated.content.tilt_adapter.TiltAdapterBlockEntity;
import dev.createpropulsionsimulated.content.wing.CopycatWingBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreatePropulsionSimulated.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThrusterBlockEntity>> THRUSTER = REGISTER.register("thruster",
            () -> BlockEntityType.Builder.of(ThrusterBlockEntity::standard, CPSBlocks.THRUSTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeThrusterBlockEntity>> CREATIVE_THRUSTER = REGISTER.register("creative_thruster",
            () -> BlockEntityType.Builder.of(CreativeThrusterBlockEntity::new, CPSBlocks.CREATIVE_THRUSTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IonThrusterBlockEntity>> ION_THRUSTER = REGISTER.register("ion_thruster",
            () -> BlockEntityType.Builder.of(IonThrusterBlockEntity::new, CPSBlocks.ION_THRUSTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopycatWingBlockEntity>> COPYCAT_WING = REGISTER.register("copycat_wing",
            () -> BlockEntityType.Builder.of(CopycatWingBlockEntity::new,
                    CPSBlocks.COPYCAT_WING.get(),
                    CPSBlocks.COPYCAT_WING_8.get(),
                    CPSBlocks.COPYCAT_WING_12.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TiltAdapterBlockEntity>> TILT_ADAPTER = REGISTER.register("tilt_adapter",
            () -> BlockEntityType.Builder.of(TiltAdapterBlockEntity::new, CPSBlocks.TILT_ADAPTER.get()).build(null));

    private CPSBlockEntities() {
    }
}
