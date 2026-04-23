package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import dev.createpropulsionsimulated.content.item.BurnableItem;
import dev.createpropulsionsimulated.content.wing.CopycatWingItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(CreatePropulsionSimulated.MOD_ID);

    public static final DeferredItem<BlockItem> THRUSTER = REGISTER.register("thruster",
            () -> new BlockItem(CPSBlocks.THRUSTER.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> CREATIVE_THRUSTER = REGISTER.register("creative_thruster",
            () -> new BlockItem(CPSBlocks.CREATIVE_THRUSTER.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> ION_THRUSTER = REGISTER.register("ion_thruster",
            () -> new BlockItem(CPSBlocks.ION_THRUSTER.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COPYCAT_WING = REGISTER.register("copycat_wing",
            () -> new CopycatWingItem(CPSBlocks.COPYCAT_WING.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COPYCAT_WING_8 = REGISTER.register("copycat_wing_8",
            () -> new BlockItem(CPSBlocks.COPYCAT_WING_8.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COPYCAT_WING_12 = REGISTER.register("copycat_wing_12",
            () -> new BlockItem(CPSBlocks.COPYCAT_WING_12.get(), new Item.Properties()));

    public static final DeferredItem<BurnableItem> PINE_RESIN = REGISTER.register("pine_resin",
            () -> new BurnableItem(new Item.Properties(), 1200));

    public static final DeferredItem<BucketItem> TURPENTINE_BUCKET = REGISTER.register("turpentine_bucket",
            () -> new BucketItem(CPSFluids.TURPENTINE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    private CPSItems() {
    }
}
