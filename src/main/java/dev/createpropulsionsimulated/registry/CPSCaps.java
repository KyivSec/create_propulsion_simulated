package dev.createpropulsionsimulated.registry;

import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlockEntity;
import dev.createpropulsionsimulated.content.thruster.IonThrusterBlockEntity;
import dev.createpropulsionsimulated.content.thruster.ThrusterBlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CPSCaps {
    private CPSCaps() {
    }

    public static void register(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CPSBlockEntities.THRUSTER.get(),
                (ThrusterBlockEntity be, Direction side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CPSBlockEntities.CREATIVE_THRUSTER.get(),
                (CreativeThrusterBlockEntity be, Direction side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CPSBlockEntities.ION_THRUSTER.get(),
                (IonThrusterBlockEntity be, Direction side) -> be.getEnergyHandler(side));
    }
}
