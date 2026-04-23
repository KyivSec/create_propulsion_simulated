package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.CreatePropulsionSimulated;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CPSCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreatePropulsionSimulated.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE = REGISTER.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createpropulsionsimulated"))
                    .icon(() -> new ItemStack(CPSItems.THRUSTER.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((parameters, output) -> {
                        output.accept(CPSItems.THRUSTER.get());
                        output.accept(CPSItems.CREATIVE_THRUSTER.get());
                        output.accept(CPSItems.COPYCAT_WING.get());
                        output.accept(CPSItems.PINE_RESIN.get());
                        output.accept(CPSItems.TURPENTINE_BUCKET.get());
                    })
                    .build());

    private CPSCreativeTabs() {
    }
}
