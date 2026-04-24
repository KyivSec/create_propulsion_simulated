package dev.createpropulsionsimulated.content.thruster;

import java.lang.reflect.Method;

public final class ThrusterSoundHooks {
    private ThrusterSoundHooks() {
    }

    public static void clientTick(final ThrusterBlockEntity blockEntity) {
        if (blockEntity.getLevel() == null || !blockEntity.getLevel().isClientSide()) {
            return;
        }

        try {
            final Class<?> controller = Class.forName("dev.createpropulsionsimulated.client.sound.ThrusterLoopSoundController");
            final Method tickMethod = controller.getMethod("tick", ThrusterBlockEntity.class);
            tickMethod.invoke(null, blockEntity);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
