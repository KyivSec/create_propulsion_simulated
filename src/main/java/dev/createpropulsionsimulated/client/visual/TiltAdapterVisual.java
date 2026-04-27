package dev.createpropulsionsimulated.client.visual;

import dev.createpropulsionsimulated.content.tilt_adapter.TiltAdapterBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;

import java.util.function.Consumer;

public class TiltAdapterVisual extends AbstractBlockEntityVisual<TiltAdapterBlockEntity> {
    public TiltAdapterVisual(VisualizationContext context, TiltAdapterBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Override
    public void updateLight(float partialTick) {
    }

    @Override
    protected void _delete() {
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
    }
}
