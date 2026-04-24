package dev.createpropulsionsimulated.content.thruster;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
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
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Locale;

public class CreativeThrusterBlockEntity extends ThrusterBlockEntity {
    private CreativeThrusterPowerScrollValueBehaviour creativePowerBehaviour;
    private PlumeType plumeType = PlumeType.PLASMA;

    public CreativeThrusterBlockEntity(final BlockPos pos, final BlockState state) {
        super(dev.createpropulsionsimulated.registry.CPSBlockEntities.CREATIVE_THRUSTER.get(), pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        this.creativePowerBehaviour = new CreativeThrusterPowerScrollValueBehaviour(this);
        this.creativePowerBehaviour.setValue(49);
        this.creativePowerBehaviour.withCallback(v -> this.sync());
        behaviours.add(this.creativePowerBehaviour);
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    protected boolean usesFuelTank() {
        return false;
    }

    @Override
    public IFluidHandler getFluidHandler(final Direction side) {
        return null;
    }

    @Override
    public boolean tryConsumeFuelBucket(final Player player,
                                        final InteractionHand hand,
                                        final ItemStack heldStack) {
        return false;
    }

    @Override
    public double getNozzleOffsetFromCenter() {
        return 0.55d;
    }

    @Override
    protected double getBaseThrust() {
        final double targetThrust = this.creativePowerBehaviour == null ? 500.0d : this.creativePowerBehaviour.getTargetThrust();
        return Math.min(targetThrust, this.getRawThrustCap());
    }

    @Override
    public float getCreativeTargetThrust() {
        final float target = this.creativePowerBehaviour == null ? 500.0f : this.creativePowerBehaviour.getTargetThrust();
        return Math.min(target, (float) this.getRawThrustCap());
    }

    @Override
    protected void recalculateObstruction(final int scanLength) {
        this.unobstructedBlocks = OBSTRUCTION_LENGTH;
    }

    @Override
    protected boolean shouldEmitParticles() {
        if (!this.isActive() || this.plumeType == PlumeType.NONE || this.level == null) {
            return false;
        }

        final Direction facing = this.getBlockState().getValue(CreativeThrusterBlock.FACING);
        final BlockPos plumeOccupiedPosition = this.worldPosition.relative(facing.getOpposite());
        return !this.level.getBlockState(plumeOccupiedPosition).isFaceSturdy(this.level, plumeOccupiedPosition, facing);
    }

    @Override
    protected LangBuilder getGoggleStatus() {
        if (this.getThrottle() > 0.0d) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.working"))
                    .style(ChatFormatting.GREEN);
        }
        return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.not_powered"))
                .style(ChatFormatting.GOLD);
    }

    @Override
    protected void addThrusterDetails(final List<Component> tooltip, final boolean isPlayerSneaking, final int unobstructed) {
        CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.thrust_output"))
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);
        CreateLang.builder()
                .add(Component.literal(" "))
                .add(Component.translatable("createpropulsionsimulated.tooltip.thrust1").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(String.format(Locale.ROOT, "%.2f", this.getDisplayedThrustPn())).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" pN").withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        CreateLang.builder()
                .add(Component.literal(" "))
                .add(Component.translatable("createpropulsionsimulated.tooltip.airflow").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(String.format(Locale.ROOT, "%.2f", this.getDisplayedAirflowMs())).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" m/s").withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);

        final LangBuilder particleBuilder = CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.creative_thruster.particle")).text(": ");
        switch (this.plumeType) {
            case PLASMA -> particleBuilder.add(CreateLang.builder()
                    .add(Component.translatable("createpropulsionsimulated.gui.goggles.creative_thruster.particle.plasma"))
                    .style(ChatFormatting.AQUA));
            case PLUME -> particleBuilder.add(CreateLang.builder()
                    .add(Component.translatable("createpropulsionsimulated.gui.goggles.creative_thruster.particle.plume"))
                    .style(ChatFormatting.GOLD));
            case NONE -> particleBuilder.add(CreateLang.builder()
                    .add(Component.translatable("createpropulsionsimulated.gui.goggles.creative_thruster.particle.none"))
                    .style(ChatFormatting.DARK_GRAY));
        }
        particleBuilder.forGoggles(tooltip);
    }

    @Override
    public void cyclePlumeType() {
        int next = this.plumeType.ordinal() + 1;
        if (next >= PlumeType.values().length) {
            next = 0;
        }
        this.plumeType = PlumeType.values()[next];
        this.sync();
    }

    @Override
    public PlumeType getPlumeType() {
        return this.plumeType;
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        tag.putInt("PlumeType", this.plumeType.ordinal());
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        if (tag.contains("PlumeType")) {
            final int index = Math.clamp(tag.getInt("PlumeType"), 0, PlumeType.values().length - 1);
            this.plumeType = PlumeType.values()[index];
        }
        super.read(tag, registries, clientPacket);
    }
}
