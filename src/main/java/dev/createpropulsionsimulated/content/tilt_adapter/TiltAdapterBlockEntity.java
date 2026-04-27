package dev.createpropulsionsimulated.content.tilt_adapter;

import dev.createpropulsionsimulated.config.ThrusterConfig;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity.SequenceContext;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import java.util.List;

public class TiltAdapterBlockEntity extends SplitShaftBlockEntity {
    public static final float SIGNAL_RANGE = 15.0f;

    protected int redstoneLeft = 0;
    protected int redstoneRight = 0;
    
    protected float targetAngle = 0f;
    protected float networkTargetAngle = 0f;
    protected float currentAngle = 0f;
    protected int activeMoveDirection = 0;
    protected float activeSequenceLimit = 0f;

    private boolean needsSync = false;

    public TiltAdapterBlockEntity(BlockPos pos, BlockState state) {
        super(dev.createpropulsionsimulated.registry.CPSBlockEntities.TILT_ADAPTER.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        if (needsSync) {
            syncNetworkState();
            needsSync = false;
        }

        float speed = Math.abs(getTheoreticalSpeed());
        if (activeMoveDirection != 0 && speed > 0 && activeSequenceLimit > 0) {
            float step = KineticBlockEntity.convertToAngular(speed);
            float actualStep = Math.min(step, activeSequenceLimit);
            
            currentAngle += actualStep * activeMoveDirection;
            activeSequenceLimit -= actualStep;

            if (activeSequenceLimit <= 0) {
                activeSequenceLimit = 0;
                currentAngle = networkTargetAngle;
                scheduleSync();
            }
        }

        checkRedstoneAndSpeed();
    }

    private void scheduleSync() {
        needsSync = true;
    }

    public int getLeft() { return redstoneLeft; }
    public int getRight() { return redstoneRight; }

    public float getCurrentAngle() {
        return currentAngle;
    }

    private void checkRedstoneAndSpeed() {
        Level level = getLevel();
        if (level == null) return;

        BlockState state = getBlockState();
        Axis axis = state.getValue(RotatedPillarKineticBlock.AXIS);
        boolean positiveDir = state.getValue(TiltAdapterBlock.POSITIVE);
        boolean alignedX = state.getValue(TiltAdapterBlock.ALIGNED_X);

        Direction posSignalSide = (axis == Axis.X) ? Direction.SOUTH : (axis == Axis.Z ? Direction.WEST : (alignedX ? Direction.NORTH : Direction.EAST));
        Direction negSignalSide = (axis == Axis.X) ? Direction.NORTH : (axis == Axis.Z ? Direction.EAST : (alignedX ? Direction.SOUTH : Direction.WEST));

        if (!positiveDir) {
            Direction temp = posSignalSide; posSignalSide = negSignalSide; negSignalSide = temp;
        }

        int newLeft = level.getSignal(worldPosition.relative(negSignalSide), negSignalSide);
        int newRight = level.getSignal(worldPosition.relative(posSignalSide), posSignalSide);
        
        if (newLeft != redstoneLeft || newRight != redstoneRight) {
            redstoneLeft = newLeft;
            redstoneRight = newRight;
            sendData();
        }

        if (getTheoreticalSpeed() == 0) {
            if (activeMoveDirection != 0) {
                scheduleSync();
            }
            return;
        }

        double maxAngle = ThrusterConfig.TILT_ADAPTER_MAX_ANGLE.get();
        int diff = redstoneLeft - redstoneRight; 
        double newTarget = (diff / SIGNAL_RANGE) * maxAngle;
        
        if (Math.abs(newTarget - targetAngle) > 0.001f) {
            targetAngle = (float)newTarget;
            scheduleSync();
        }
    }

    private void syncNetworkState() {
        float speed = Math.abs(getTheoreticalSpeed());
        float delta = targetAngle - currentAngle;

        if (Math.abs(delta) > 0.001f && speed > 0) {
            activeMoveDirection = (int) Math.signum(delta);
            activeSequenceLimit = Math.abs(delta);
            networkTargetAngle = targetAngle;
            
            float kineticSpeed = speed * activeMoveDirection;
            sequenceContext = new SequenceContext(SequencerInstructions.TURN_ANGLE, activeSequenceLimit / Math.abs(kineticSpeed));
        } else {
            activeMoveDirection = 0;
            activeSequenceLimit = 0;
            sequenceContext = null;
            currentAngle = targetAngle;
        }

        detachKinetics();
        attachKinetics();
        sendData();
    }

    @Override
    public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
        Direction directionToTarget = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
        if (directionToTarget == getBackFace(stateFrom)) return 0;
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (face == TiltAdapterBlock.getDirection(getBlockState())) return activeMoveDirection;
        if (hasSource() && getSourceFacing() != getBackFace(getBlockState())) return 0;
        return 1;
    }

    private Direction getBackFace(BlockState state) {
        Axis axis = state.getValue(RotatedPillarKineticBlock.AXIS);
        boolean positive = state.hasProperty(TiltAdapterBlock.POSITIVE) ? state.getValue(TiltAdapterBlock.POSITIVE) : true;
        return Direction.get(positive ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE, axis);
    }

    @Override
    public void initialize() {
        super.initialize();
        Level level = getLevel();
        if (level != null && !level.isClientSide) scheduleSync();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putFloat("targetAngle", targetAngle);
        compound.putFloat("networkTargetAngle", networkTargetAngle);
        compound.putFloat("currentAngle", currentAngle);
        compound.putInt("activeMoveDirection", activeMoveDirection);
        compound.putFloat("activeSequenceLimit", activeSequenceLimit);
        compound.putInt("redstoneLeft", redstoneLeft);
        compound.putInt("redstoneRight", redstoneRight);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        targetAngle = compound.getFloat("targetAngle");
        networkTargetAngle = compound.getFloat("networkTargetAngle");
        currentAngle = compound.getFloat("currentAngle");
        activeMoveDirection = compound.getInt("activeMoveDirection");
        activeSequenceLimit = compound.getFloat("activeSequenceLimit");
        redstoneLeft = compound.getInt("redstoneLeft");
        redstoneRight = compound.getInt("redstoneRight");
        super.read(compound, registries, clientPacket);
    }

    @Override protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {}
    @Override protected boolean syncSequenceContext() { return true; }
}
