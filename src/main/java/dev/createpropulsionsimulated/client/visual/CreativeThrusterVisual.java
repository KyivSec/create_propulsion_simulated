package dev.createpropulsionsimulated.client.visual;

import dev.createpropulsionsimulated.client.CPSPartialModels;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlock;
import dev.createpropulsionsimulated.content.thruster.CreativeThrusterBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class CreativeThrusterVisual extends AbstractBlockEntityVisual<CreativeThrusterBlockEntity> implements SimpleDynamicVisual {
    private OrientedInstance bracket;

    public CreativeThrusterVisual(final VisualizationContext context, final CreativeThrusterBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);

        final Direction facing = this.blockState.getValue(CreativeThrusterBlock.FACING);
        final Direction placementFacing = this.blockState.getValue(CreativeThrusterBlock.PLACEMENT_FACING);
        if (facing.getAxis() == placementFacing.getAxis()) {
            this.bracket = null;
            return;
        }

        this.bracket = this.instancerProvider()
                .instancer(InstanceTypes.ORIENTED, Models.partial(CPSPartialModels.CREATIVE_THRUSTER_BRACKET))
                .createInstance();

        final float angle = this.getBracketAngle(facing, placementFacing);
        final Quaternionf q = new Quaternionf(facing.getRotation());
        q.mul(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 90));
        q.mul(new Quaternionf().rotationZ(Mth.DEG_TO_RAD * angle));

        this.bracket.position(this.getVisualPosition())
                .rotation(q)
                .light(this.computePackedLight())
                .setChanged();
    }

    private float getBracketAngle(final Direction facing, final Direction placementFacing) {
        final Vector3f local = new Vector3f(placementFacing.step());
        final Quaternionf q = new Quaternionf(facing.getRotation()).conjugate();
        local.rotate(q);
        local.rotateX((float) Math.toRadians(-90));
        final double targetAngle = Math.toDegrees(Math.atan2(local.y, local.x));
        return (float) (targetAngle + 90);
    }

    @Override
    public void beginFrame(final Context context) {
    }

    @Override
    public void updateLight(final float partialTick) {
        if (this.bracket != null) {
            this.bracket.light(this.computePackedLight()).setChanged();
        }
    }

    @Override
    protected void _delete() {
        if (this.bracket != null) {
            this.bracket.delete();
        }
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        if (this.bracket != null) {
            consumer.accept(this.bracket);
        }
    }
}
