package dev.createpropulsionsimulated.content.thruster;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.createpropulsionsimulated.config.ThrusterConfig;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class IonThrusterBlockEntity extends ThrusterBlockEntity {
    private static final double ION_MAX_THRUST_PN = 1000.0d;
    private int energyStored;
    private double energyDrainAccumulator;

    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }
            final int accepted = Math.min(maxReceive, Math.max(0, getEnergyCapacity() - energyStored));
            if (!simulate && accepted > 0) {
                energyStored += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(final int maxExtract, final boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return getEnergyCapacity();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public IonThrusterBlockEntity(final BlockPos pos, final BlockState state) {
        super(CPSBlockEntities.ION_THRUSTER.get(), pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public boolean isIon() {
        return true;
    }

    @Override
    protected boolean usesFuelTank() {
        return false;
    }

    @Override
    protected boolean requiresResourceForThrust() {
        return true;
    }

    @Override
    protected boolean tickResourceAndGetAvailability(final boolean powered, final double throttle) {
        this.clampEnergyToCapacity();
        if (!powered) {
            this.energyDrainAccumulator = 0.0d;
            return this.energyStored > 0;
        }

        this.energyDrainAccumulator += throttle * ThrusterConfig.ION_THRUSTER_FE_PER_TICK_AT_FULL_THROTTLE.get();
        while (this.energyDrainAccumulator >= 1.0d && this.energyStored > 0) {
            this.energyStored--;
            this.energyDrainAccumulator -= 1.0d;
        }
        return this.energyStored > 0;
    }

    @Override
    public IFluidHandler getFluidHandler(final Direction side) {
        return null;
    }

    public IEnergyStorage getEnergyHandler(final Direction side) {
        if (side != this.getEnergyInputSide()) {
            return null;
        }
        return this.energyHandler;
    }

    @Override
    public boolean isVisuallyActive() {
        return this.getThrottle() > 0.0d && this.energyStored > 0;
    }

    @Override
    public boolean tryConsumeFuelBucket(final Player player, final InteractionHand hand, final ItemStack heldStack) {
        return false;
    }

    @Override
    protected double getBaseThrust() {
        return Math.min(ThrusterConfig.ION_THRUSTER_BASE_THRUST.get(), this.getRawThrustCap());
    }

    @Override
    protected double getRawThrustCap() {
        return ION_MAX_THRUST_PN;
    }

    public int getEnergyStoredFe() {
        return this.energyStored;
    }

    public int getEnergyCapacity() {
        return ThrusterConfig.ION_THRUSTER_ENERGY_CAPACITY_FE.get();
    }

    protected Direction getEnergyInputSide() {
        return this.getFacing();
    }

    @Override
    protected LangBuilder getGoggleStatus() {
        if (this.getThrottle() <= 0.0d) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.not_powered"))
                    .style(ChatFormatting.GOLD);
        }
        if (this.energyStored <= 0) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.no_energy"))
                    .style(ChatFormatting.RED);
        }
        return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.working"))
                .style(ChatFormatting.GREEN);
    }

    @Override
    protected void addThrusterDetails(final List<Component> tooltip, final boolean isPlayerSneaking, final int unobstructed) {
        super.addThrusterDetails(tooltip, isPlayerSneaking, unobstructed);
        CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.energy_container"))
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);
        CreateLang.builder()
                .add(Component.literal(" "))
                .add(Component.literal(Integer.toString(this.energyStored)).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(Integer.toString(this.getEnergyCapacity())).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" FE").withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        tag.putInt("EnergyStored", this.energyStored);
        tag.putDouble("EnergyDrainAccumulator", this.energyDrainAccumulator);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.energyStored = tag.getInt("EnergyStored");
        this.energyDrainAccumulator = tag.getDouble("EnergyDrainAccumulator");
        this.clampEnergyToCapacity();
        super.read(tag, registries, clientPacket);
    }

    private void clampEnergyToCapacity() {
        this.energyStored = Math.clamp(this.energyStored, 0, this.getEnergyCapacity());
    }
}
