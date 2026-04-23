package dev.createpropulsionsimulated.content.thruster;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.createpropulsionsimulated.config.ThrusterConfig;
import dev.createpropulsionsimulated.debug.CPSDebugManager;
import dev.createpropulsionsimulated.registry.CPSBlockEntities;
import dev.createpropulsionsimulated.registry.CPSFluids;
import dev.createpropulsionsimulated.registry.CPSItems;
import dev.createpropulsionsimulated.utility.GoggleUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

public class ThrusterBlockEntity extends SmartBlockEntity implements BlockEntitySubLevelActor, IHaveGoggleInformation {
    protected static final int MB_PER_BUCKET = 1000;
    protected static final int OBSTRUCTION_LENGTH = ThrusterState.FULL_EFFICIENCY_AIR_GAP;
    protected static final float LOWEST_POWER_THRESHOLD = 5.0f / 15.0f;
    protected static final double STANDARD_MAX_THRUST_PN = 600.0d;
    protected static final double CREATIVE_MAX_THRUST_PN = 1000.0d;
    private static final double EARTH_GRAVITY = 9.81d;

    protected SmartFluidTankBehaviour tank;
    protected int unobstructedBlocks;
    protected int redstonePower;

    private int tickCounter;
    private double currentThrust;
    private double fluidDrainAccumulator;
    private double displayedThrustPn;
    private double displayedAirflowMs;

    public enum PlumeType {
        PLASMA,
        PLUME,
        NONE
    }

    public ThrusterBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState blockState) {
        super(type, pos, blockState);
    }

    public static ThrusterBlockEntity standard(final BlockPos pos, final BlockState state) {
        return new ThrusterBlockEntity(CPSBlockEntities.THRUSTER.get(), pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        this.tank = SmartFluidTankBehaviour.single(this, ThrusterConfig.FUEL_TANK_CAPACITY_MB.get())
                .allowInsertion();
        this.tank.getPrimaryHandler().setValidator(stack -> stack.getFluid().isSame(CPSFluids.TURPENTINE.get()));
        behaviours.add(this.tank);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.level != null && !this.level.isClientSide()) {
            this.setRedstonePower(this.level.getBestNeighborSignal(this.worldPosition));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level == null) {
            return;
        }

        if (this.level.isClientSide()) {
            ThrusterSoundHooks.clientTick(this);
            ThrusterParticles.spawn(this);
            return;
        }
        if (this.usesFuelTank() && this.tank == null) {
            return;
        }

        this.tickCounter++;
        final double previousThrust = this.currentThrust;
        final boolean previouslyActive = previousThrust > 0.0d;
        final double throttle = this.getThrottle();
        final boolean powered = throttle > 0.0d;
        final int scanLength = ThrusterConfig.OBSTRUCTION_SCAN_LENGTH.get();
        if (powered || this.tickCounter == 1 || this.tickCounter % 10 == 0) {
            this.recalculateObstruction(scanLength);
        }
        final boolean hasResource = this.tickResourceAndGetAvailability(powered, throttle);
        final boolean canProduce = ThrusterState.canProduceThrust(powered, hasResource, this.requiresResourceForThrust());
        if (canProduce) {
            final double efficiency = ThrusterState.obstructionEfficiency(this.unobstructedBlocks);
            this.currentThrust = Math.min(ThrusterState.thrust(this.getBaseThrust(), throttle, efficiency), this.getRawThrustCap());
        } else {
            this.currentThrust = 0.0d;
        }
        this.displayedThrustPn = this.currentThrust;
        this.displayedAirflowMs = 0.0d;

        if (this.isActive() && ThrusterConfig.DAMAGE_ENTITIES.get()) {
            final int interval = ThrusterConfig.DAMAGE_TICK_INTERVAL.get();
            if (interval > 0 && this.tickCounter % interval == 0) {
                this.damageEntitiesInPlume();
            }
        }

        final boolean activeChanged = (this.currentThrust > 0.0d) != previouslyActive;
        final boolean thrustChangedNoticeably = Math.abs(this.currentThrust - previousThrust) >= 1.0d;
        if (activeChanged || (thrustChangedNoticeably && this.tickCounter % 4 == 0) || this.tickCounter % 10 == 0) {
            this.sync();
        }

        this.emitDebugForceVector(this.level);
    }

    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        if (this.currentThrust <= 0.0d) {
            return;
        }

        final ThrusterForceProvider.ForceSample sample = ThrusterForceProvider.createSample(this, timeStep);
        final double scaledThrust = this.getScaledThrust(subLevel);
        if (scaledThrust <= 0.0d || !Double.isFinite(scaledThrust)) {
            return;
        }
        this.displayedThrustPn = scaledThrust;
        final Vector3d worldDirection = subLevel.logicalPose()
                .transformNormal(new Vector3d(sample.directionLocal()))
                .normalize();
        final Vector3d worldVelocity = handle.getLinearVelocity(new Vector3d());
        this.displayedAirflowMs = Math.abs(worldVelocity.dot(worldDirection));

        final double thrustScale = scaledThrust / this.currentThrust;
        final Vector3d scaledImpulse = new Vector3d(sample.impulseLocal()).mul(thrustScale);
        final Vector3d limitedImpulse = this.limitImpulseBySpeed(subLevel, handle, scaledImpulse);
        if (limitedImpulse.lengthSquared() <= 1.0e-7d) {
            return;
        }
        SimulatedThrustAdapter.applyImpulseAtPoint(subLevel, sample.pointLocal(), limitedImpulse);

        final Vector3d groundFrictionImpulse = this.computeGroundFrictionImpulse(subLevel, handle, timeStep);
        if (groundFrictionImpulse.lengthSquared() > 1.0e-7d) {
            final QueuedForceGroup dragGroup = subLevel.getOrCreateQueuedForceGroup(ForceGroups.DRAG.get());
            dragGroup.getForceTotal().applyLinearImpulse(groundFrictionImpulse);
        }
    }

    public Direction getFacing() {
        return this.getBlockState().getValue(ThrusterBlock.FACING);
    }

    public void setRedstonePower(final int redstonePower) {
        final int clamped = Math.clamp(redstonePower, 0, 15);
        if (this.redstonePower != clamped) {
            this.redstonePower = clamped;
            this.sync();
        }
    }

    public double getThrottle() {
        return ThrusterState.throttle(this.redstonePower);
    }

    public double getCurrentThrust() {
        return this.currentThrust;
    }

    public boolean isActive() {
        return this.currentThrust > 0.0d;
    }

    public boolean isCreative() {
        return false;
    }

    public int getFuelAmountMb() {
        return this.tank == null ? 0 : this.tank.getPrimaryHandler().getFluidAmount();
    }

    public int getFuelCapacityMb() {
        return ThrusterConfig.FUEL_TANK_CAPACITY_MB.get();
    }

    public int getUnobstructedBlocks() {
        return this.unobstructedBlocks;
    }

    public int getObstructionLength() {
        return OBSTRUCTION_LENGTH;
    }

    public double getDisplayedThrustPnForTooltip() {
        return this.getDisplayedThrustPn();
    }

    public double getDisplayedAirflowMsForTooltip() {
        return this.getDisplayedAirflowMs();
    }

    public double getNozzleOffsetFromCenter() {
        return 0.95d;
    }

    public IFluidHandler getFluidHandler(final Direction side) {
        if (!this.usesFuelTank() || this.tank == null || side != this.getFacing()) {
            return null;
        }
        return this.tank.getCapability();
    }

    public boolean tryConsumeFuelBucket(final Player player,
                                        final InteractionHand hand,
                                        final ItemStack heldStack) {
        if (!this.usesFuelTank() || this.tank == null) {
            return false;
        }
        if (!heldStack.is(CPSItems.TURPENTINE_BUCKET.get())) {
            return false;
        }
        if (this.level == null || this.level.isClientSide()) {
            return true;
        }

        final FluidStack stack = new FluidStack(CPSFluids.TURPENTINE.get(), MB_PER_BUCKET);
        final int simulated = this.tank.getPrimaryHandler().fill(stack, IFluidHandler.FluidAction.SIMULATE);
        if (simulated < MB_PER_BUCKET) {
            return false;
        }

        this.tank.getPrimaryHandler().fill(stack, IFluidHandler.FluidAction.EXECUTE);
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
            final ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
            if (heldStack.isEmpty()) {
                player.setItemInHand(hand, emptyBucket);
            } else if (!player.getInventory().add(emptyBucket)) {
                player.drop(emptyBucket, false);
            }
        }

        this.sync();
        return true;
    }

    protected double getBaseThrust() {
        return Math.min(ThrusterConfig.BASE_THRUST.get(), this.getRawThrustCap());
    }

    protected double getRawThrustCap() {
        return this.isCreative() ? CREATIVE_MAX_THRUST_PN : STANDARD_MAX_THRUST_PN;
    }

    public float getCreativeTargetThrust() {
        return 0.0f;
    }

    protected void recalculateObstruction(final int scanLength) {
        if (this.level == null) {
            this.unobstructedBlocks = 0;
            return;
        }

        final Direction exhaustDirection = this.getFacing().getOpposite();
        int clearCount = 0;
        for (int i = 1; i <= scanLength; i++) {
            final BlockPos checkPos = this.worldPosition.relative(exhaustDirection, i);
            final BlockState checkState = this.level.getBlockState(checkPos);
            if (checkState.getCollisionShape(this.level, checkPos).isEmpty()) {
                clearCount++;
            } else {
                break;
            }
        }
        this.unobstructedBlocks = clearCount;
    }

    protected boolean shouldEmitParticles() {
        return this.isVisuallyActive();
    }

    public boolean isVisuallyActive() {
        if (this.getThrottle() <= 0.0d) {
            return false;
        }
        if (this.isCreative()) {
            return true;
        }
        if (!this.usesFuelTank()) {
            return true;
        }
        return this.getFuelAmountMb() > 0;
    }

    protected boolean usesFuelTank() {
        return true;
    }

    protected boolean requiresResourceForThrust() {
        return ThrusterConfig.REQUIRE_FUEL.get() && this.usesFuelTank();
    }

    protected boolean tickResourceAndGetAvailability(final boolean powered, final double throttle) {
        final boolean usesTank = this.usesFuelTank();
        final boolean requireFuel = ThrusterConfig.REQUIRE_FUEL.get();
        if (usesTank && this.tank == null) {
            return false;
        }

        if (powered && requireFuel && usesTank) {
            this.fluidDrainAccumulator += throttle * ThrusterConfig.FUEL_MB_PER_TICK_AT_FULL_THROTTLE.get();
            while (this.fluidDrainAccumulator >= 1.0d) {
                final FluidStack drained = this.tank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
                if (drained.isEmpty()) {
                    break;
                }
                this.fluidDrainAccumulator -= 1.0d;
            }
        } else if (!powered) {
            this.fluidDrainAccumulator = 0.0d;
        }

        return !usesTank || !this.tank.getPrimaryHandler().isEmpty();
    }

    public boolean isIon() {
        return false;
    }

    public void cyclePlumeType() {
    }

    public PlumeType getPlumeType() {
        return PlumeType.PLUME;
    }

    protected LangBuilder getGoggleStatus() {
        if (this.tank == null || this.tank.getPrimaryHandler().isEmpty()) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.no_fuel"))
                    .style(ChatFormatting.RED);
        }

        final FluidStack fuel = this.tank.getPrimaryHandler().getFluidInTank(0);
        if (!fuel.getFluid().isSame(CPSFluids.TURPENTINE.get())) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.wrong_fuel"))
                    .style(ChatFormatting.RED);
        }

        if (this.getThrottle() <= 0.0d) {
            return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.not_powered"))
                    .style(ChatFormatting.GOLD);
        }

        return CreateLang.builder().add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status.working"))
                .style(ChatFormatting.GREEN);
    }

    protected void addThrusterDetails(final List<Component> tooltip, final boolean isPlayerSneaking, final int unobstructed) {
        float efficiency = 100.0f;
        ChatFormatting tooltipColor = ChatFormatting.GREEN;
        final int clamped = Math.clamp(unobstructed, 0, OBSTRUCTION_LENGTH);
        final int roundedEfficiency = Math.clamp(Math.round((clamped / (float) OBSTRUCTION_LENGTH) * 10.0f) * 10, 0, 100);
        if (clamped < OBSTRUCTION_LENGTH) {
            efficiency = (clamped / (float) OBSTRUCTION_LENGTH) * 100.0f;
            tooltipColor = GoggleUtils.efficiencyColor(efficiency);
            CreateLang.builder()
                    .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.obstructed"))
                    .space()
                    .add(CreateLang.text(GoggleUtils.makeObstructionBar(clamped, OBSTRUCTION_LENGTH)))
                    .style(tooltipColor)
                    .forGoggles(tooltip);
        }

        CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.efficiency"))
                .text(": ")
                .add(CreateLang.number(roundedEfficiency))
                .add(CreateLang.text("%"))
                .style(tooltipColor)
                .forGoggles(tooltip);

        CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.thrust_output"))
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

        CreateLang.builder()
                .add(Component.literal(" "))
                .add(Component.literal("Thrust: ").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(String.format(Locale.ROOT, "%.2f", this.getDisplayedThrustPn())).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" pN").withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);

        CreateLang.builder()
                .add(Component.literal(" "))
                .add(Component.literal("Airflow: ").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(String.format(Locale.ROOT, "%.2f", this.getDisplayedAirflowMs())).withStyle(ChatFormatting.AQUA))
                .add(Component.literal(" m/s").withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);

        if (this.usesFuelTank()) {
            final int capacity = ThrusterConfig.FUEL_TANK_CAPACITY_MB.get();
            final int current = this.getFuelAmountMb();
            CreateLang.builder()
                    .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.fluid_container"))
                    .style(ChatFormatting.WHITE)
                    .forGoggles(tooltip);
            CreateLang.builder()
                    .text(" ")
                    .add(Component.translatable("fluid.createpropulsionsimulated.turpentine"))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.builder()
                    .add(Component.literal(" "))
                    .add(Component.literal(Integer.toString(current)).withStyle(ChatFormatting.AQUA))
                    .add(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                    .add(Component.literal(Integer.toString(capacity)).withStyle(ChatFormatting.AQUA))
                    .add(Component.literal(" mB").withStyle(ChatFormatting.GRAY))
                    .forGoggles(tooltip);
        }
    }

    protected double getDisplayedThrustPn() {
        if (Double.isFinite(this.displayedThrustPn) && this.displayedThrustPn > 0.0d) {
            return this.displayedThrustPn;
        }
        return Math.max(0.0d, this.currentThrust);
    }

    protected double getDisplayedAirflowMs() {
        if (Double.isFinite(this.displayedAirflowMs) && this.displayedAirflowMs > 0.0d) {
            return this.displayedAirflowMs;
        }
        return 0.0d;
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        final int oldUnobstructed = this.unobstructedBlocks;
        this.recalculateObstruction(OBSTRUCTION_LENGTH);
        final int recalculated = this.unobstructedBlocks;
        this.unobstructedBlocks = oldUnobstructed;

        CreateLang.builder()
                .add(Component.translatable("createpropulsionsimulated.gui.goggles.thruster.status"))
                .text(":")
                .space()
                .add(this.getGoggleStatus())
                .forGoggles(tooltip);

        this.addThrusterDetails(tooltip, isPlayerSneaking, recalculated);
        return true;
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        tag.putInt("RedstonePower", this.redstonePower);
        tag.putInt("UnobstructedBlocks", this.unobstructedBlocks);
        tag.putInt("TickCounter", this.tickCounter);
        tag.putDouble("CurrentThrust", this.currentThrust);
        tag.putDouble("FluidDrainAccumulator", this.fluidDrainAccumulator);
        tag.putDouble("DisplayedThrustPn", this.displayedThrustPn);
        tag.putDouble("DisplayedAirflowMs", this.displayedAirflowMs);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.redstonePower = tag.getInt("RedstonePower");
        this.unobstructedBlocks = tag.getInt("UnobstructedBlocks");
        this.tickCounter = tag.getInt("TickCounter");
        this.currentThrust = tag.getDouble("CurrentThrust");
        this.fluidDrainAccumulator = tag.getDouble("FluidDrainAccumulator");
        this.displayedThrustPn = tag.getDouble("DisplayedThrustPn");
        this.displayedAirflowMs = tag.getDouble("DisplayedAirflowMs");
        super.read(tag, registries, clientPacket);
    }

    protected void sync() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void damageEntitiesInPlume() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        final float power = (float) this.getThrottle();
        if (power < LOWEST_POWER_THRESHOLD) {
            return;
        }

        final float visualPowerPercent = Math.max(0.0f, power - LOWEST_POWER_THRESHOLD) / (1.0f - LOWEST_POWER_THRESHOLD);
        final double distanceByPower = org.joml.Math.lerp(0.55f, 1.5f, visualPowerPercent);
        final double potentialPlumeLength = this.unobstructedBlocks * distanceByPower;
        if (potentialPlumeLength <= 0.01d) {
            return;
        }

        final Direction exhaust = this.getFacing().getOpposite();
        final double nozzleOffset = this.getNozzleOffsetFromCenter();
        final Vec3 localNozzle = new Vec3(
                this.worldPosition.getX() + 0.5d + exhaust.getStepX() * nozzleOffset,
                this.worldPosition.getY() + 0.5d + exhaust.getStepY() * nozzleOffset,
                this.worldPosition.getZ() + 0.5d + exhaust.getStepZ() * nozzleOffset
        );
        final SimulatedThrustAdapter.Projection projection = SimulatedThrustAdapter.projectToWorld(
                this.level,
                this.worldPosition,
                new Vector3d(localNozzle.x, localNozzle.y, localNozzle.z),
                new Vector3d(exhaust.getStepX(), exhaust.getStepY(), exhaust.getStepZ())
        );

        final Vec3 nozzlePos = projection.position();
        final Vec3 dir = projection.direction().normalize();
        final Vec3 rayEnd = nozzlePos.add(dir.scale(potentialPlumeLength));
        final BlockHitResult hit = this.level.clip(new ClipContext(
                nozzlePos,
                rayEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                (Entity) null
        ));

        final double correctedLength = hit.getType() == BlockHitResult.Type.BLOCK
                ? nozzlePos.distanceTo(hit.getLocation())
                : potentialPlumeLength;
        if (correctedLength <= 0.01d) {
            return;
        }

        final double plumeStartOffset = 0.8d;
        final Vec3 plumeStart = nozzlePos.add(dir.scale(plumeStartOffset));
        final Vec3 plumeEnd = nozzlePos.add(dir.scale(plumeStartOffset + correctedLength));
        final AABB queryBox = new AABB(plumeStart, plumeEnd).inflate(0.7d);
        final DamageSource damageSource = this.level.damageSources().onFire();

        for (final LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, queryBox)) {
            final Vec3 entityPos = entity.position();
            final double distanceSqToSegment = distanceSqPointToSegment(entityPos, plumeStart, plumeEnd);
            if (distanceSqToSegment > 0.7d * 0.7d) {
                continue;
            }

            final float invSqrDistance = (visualPowerPercent * 15.0f) * 8.0f
                    / (float) Math.max(1.0d, entityPos.distanceToSqr(nozzlePos));
            final float damageAmount = 3.0f + invSqrDistance;
            entity.hurt(damageSource, damageAmount);
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 60));
        }
    }

    private static double distanceSqPointToSegment(final Vec3 point, final Vec3 a, final Vec3 b) {
        final Vec3 ab = b.subtract(a);
        final double abLenSq = ab.lengthSqr();
        if (abLenSq < 1.0e-7d) {
            return point.distanceToSqr(a);
        }
        final double t = Math.clamp(point.subtract(a).dot(ab) / abLenSq, 0.0d, 1.0d);
        final Vec3 closest = a.add(ab.scale(t));
        return point.distanceToSqr(closest);
    }

    private double getScaledThrust(final ServerSubLevel subLevel) {
        final double rawThrust = this.currentThrust;
        if (rawThrust <= 0.0d || !Double.isFinite(rawThrust) || this.level == null) {
            return 0.0d;
        }

        final double airPressure = DimensionPhysicsData.getAirPressure(
                this.level,
                Sable.HELPER.projectOutOfSubLevel(this.level, JOMLConversion.atCenterOf(this.worldPosition))
        );
        final double airflowScaling = this.getAirflowScaling(subLevel);
        if (!Double.isFinite(airPressure) || !Double.isFinite(airflowScaling)) {
            return 0.0d;
        }

        return rawThrust * airPressure * airflowScaling;
    }

    private double getAirflowScaling(final ServerSubLevel subLevel) {
        final double airflow = this.currentThrust;
        if (Math.abs(airflow) <= 0.001d || this.level == null) {
            return 1.0d;
        }

        final Vector3d localPos = JOMLConversion.atCenterOf(this.worldPosition);
        final Vector3d velocity = Sable.HELPER.getVelocity(this.level, subLevel, localPos, new Vector3d());
        final Vector3d thrustDirection = subLevel.logicalPose().transformNormal(
                new Vector3d(this.getFacing().getStepX(), this.getFacing().getStepY(), this.getFacing().getStepZ())
        );
        final double scaling = (airflow + velocity.dot(thrustDirection)) / airflow;
        return Math.clamp(scaling, 0.0d, 1.0d);
    }

    private Vector3d limitImpulseBySpeed(final ServerSubLevel subLevel, final RigidBodyHandle handle, final Vector3d impulseLocal) {
        final int maxSpeed = this.isCreative()
                ? ThrusterConfig.CREATIVE_THRUSTER_MAX_SPEED.get()
                : ThrusterConfig.THRUSTER_MAX_SPEED.get();
        if (maxSpeed <= 0) {
            return new Vector3d(impulseLocal);
        }

        if (!Double.isFinite(impulseLocal.x) || !Double.isFinite(impulseLocal.y) || !Double.isFinite(impulseLocal.z)) {
            return new Vector3d();
        }

        final double mass = subLevel.getMassTracker().getMass();
        if (!Double.isFinite(mass) || mass <= 1.0e-7d) {
            return new Vector3d();
        }

        final Vector3d linearVelocity = handle.getLinearVelocity(new Vector3d());
        if (!Double.isFinite(linearVelocity.x) || !Double.isFinite(linearVelocity.y) || !Double.isFinite(linearVelocity.z)) {
            return new Vector3d();
        }

        final Vector3d worldImpulse = subLevel.logicalPose().transformNormal(new Vector3d(impulseLocal));
        if (!Double.isFinite(worldImpulse.x) || !Double.isFinite(worldImpulse.y) || !Double.isFinite(worldImpulse.z)) {
            return new Vector3d();
        }

        final Vector3d deltaV = new Vector3d(worldImpulse).div(mass);
        final Vector3d projectedVelocity = new Vector3d(linearVelocity).add(deltaV);
        final double projectedSpeed = projectedVelocity.length();
        if (!Double.isFinite(projectedSpeed)) {
            return new Vector3d();
        }
        if (projectedSpeed <= maxSpeed) {
            return new Vector3d(impulseLocal);
        }

        final double currentSpeed = linearVelocity.length();
        if (!Double.isFinite(currentSpeed)) {
            return new Vector3d();
        }
        if (currentSpeed >= maxSpeed) {
            final double speedDelta = projectedSpeed - currentSpeed;
            if (speedDelta > 1.0e-6d) {
                return new Vector3d();
            }
            return new Vector3d(impulseLocal);
        }

        final double remainingBudget = maxSpeed - currentSpeed;
        final double requestedIncrease = projectedSpeed - currentSpeed;
        if (requestedIncrease <= 1.0e-6d) {
            return new Vector3d(impulseLocal);
        }

        final double scale = Math.clamp(remainingBudget / requestedIncrease, 0.0d, 1.0d);
        if (scale <= 1.0e-6d) {
            return new Vector3d();
        }
        return new Vector3d(impulseLocal).mul(scale);
    }

    private Vector3d computeGroundFrictionImpulse(final ServerSubLevel subLevel,
                                                  final RigidBodyHandle handle,
                                                  final double timeStep) {
        if (!this.isGrounded(subLevel) || timeStep <= 1.0e-7d) {
            return new Vector3d();
        }

        final Vector3d worldVelocity = handle.getLinearVelocity(new Vector3d());
        if (!Double.isFinite(worldVelocity.x) || !Double.isFinite(worldVelocity.y) || !Double.isFinite(worldVelocity.z)) {
            return new Vector3d();
        }

        final Vector3d horizontalVelocity = new Vector3d(worldVelocity.x, 0.0d, worldVelocity.z);
        final double speed = horizontalVelocity.length();
        if (!Double.isFinite(speed) || speed <= ThrusterConfig.GROUNDED_SPEED_DEADZONE.get()) {
            return new Vector3d();
        }

        final double mass = subLevel.getMassTracker().getMass();
        if (!Double.isFinite(mass) || mass <= 1.0e-7d) {
            return new Vector3d();
        }

        final double frictionCoefficient = Math.max(ThrusterConfig.GROUND_FRICTION_COEFFICIENT.get(), 0.08d);
        final double linearDrag = Math.max(ThrusterConfig.GROUND_LINEAR_DRAG.get(), 180.0d);
        final double rollingResistance = Math.max(ThrusterConfig.GROUND_ROLLING_RESISTANCE.get(), 80.0d);

        final double maxGroundFriction = frictionCoefficient * mass * EARTH_GRAVITY;
        final double dynamicResistance = linearDrag * speed + rollingResistance;
        double forceMagnitude = Math.min(maxGroundFriction, dynamicResistance);
        if (!Double.isFinite(forceMagnitude) || forceMagnitude <= 0.0d) {
            return new Vector3d();
        }

        final double maxStoppingImpulse = mass * speed;
        final double requestedImpulse = forceMagnitude * timeStep;
        if (requestedImpulse > maxStoppingImpulse) {
            forceMagnitude = maxStoppingImpulse / timeStep;
        }

        final Vector3d worldFrictionDirection = horizontalVelocity.normalize(new Vector3d()).negate();
        final Vector3d worldFrictionImpulse = worldFrictionDirection.mul(forceMagnitude * timeStep);
        if (!Double.isFinite(worldFrictionImpulse.x) || !Double.isFinite(worldFrictionImpulse.y) || !Double.isFinite(worldFrictionImpulse.z)) {
            return new Vector3d();
        }

        return subLevel.logicalPose().transformNormalInverse(worldFrictionImpulse);
    }

    private boolean isGrounded(final ServerSubLevel subLevel) {
        final Level globalLevel = subLevel.getLevel();
        if (globalLevel == null) {
            return false;
        }

        final Vector3d worldCenter = subLevel.logicalPose().transformPosition(JOMLConversion.atCenterOf(this.worldPosition), new Vector3d());
        final double probeDistance = ThrusterConfig.GROUND_PROBE_DISTANCE.get();
        final Vec3 probeStart = new Vec3(worldCenter.x, worldCenter.y - 0.55d, worldCenter.z);
        final Vec3 probeEnd = probeStart.add(0.0d, -probeDistance, 0.0d);
        final BlockHitResult hit = globalLevel.clip(new ClipContext(
                probeStart,
                probeEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                (Entity) null
        ));
        return hit.getType() == BlockHitResult.Type.BLOCK;
    }

    private void emitDebugForceVector(final Level level) {
        if (!(level instanceof final net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        if (!this.isActive() || !CPSDebugManager.hasWatchersNear(serverLevel, this.getBlockPos(), 64.0d)) {
            return;
        }

        final ThrusterForceProvider.ForceSample sample = ThrusterForceProvider.createSample(this, 1.0d / 20.0d);
        final SimulatedThrustAdapter.Projection projection = SimulatedThrustAdapter.projectToWorld(level, this.getBlockPos(), sample.pointLocal(), sample.directionLocal());
        final double length = Math.clamp(this.currentThrust / (this.getBaseThrust() + 1.0d), 0.1d, 1.0d);

        final var pos = projection.position();
        final var dir = projection.direction().normalize();
        final DustParticleOptions particle = new DustParticleOptions(new Vector3f(0.95f, 0.2f, 0.1f), 1.0f);
        for (int i = 0; i < 8; i++) {
            final double t = i / 7.0d;
            final double px = pos.x + dir.x * length * t;
            final double py = pos.y + dir.y * length * t;
            final double pz = pos.z + dir.z * length * t;
            serverLevel.sendParticles(particle, px, py, pz, 1, 0.0d, 0.0d, 0.0d, 0.0d);
        }
    }
}
