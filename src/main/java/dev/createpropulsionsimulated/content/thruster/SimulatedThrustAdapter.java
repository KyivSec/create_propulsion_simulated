package dev.createpropulsionsimulated.content.thruster;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public final class SimulatedThrustAdapter {
    private SimulatedThrustAdapter() {
    }

    public static void applyImpulseAtPoint(final ServerSubLevel subLevel, final Vector3d pointLocal, final Vector3d impulseLocal) {
        final QueuedForceGroup forceGroup = subLevel.getOrCreateQueuedForceGroup(ForceGroups.PROPULSION.get());
        forceGroup.applyAndRecordPointForce(pointLocal, impulseLocal);
    }

    public static @Nullable ServerSubLevel resolveServerSubLevel(final Level level, final BlockPos pos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel serverSubLevel) {
            return serverSubLevel;
        }
        return null;
    }

    public static Projection projectToWorld(final Level level, final BlockPos pos, final Vector3d localPosition, final Vector3d localDirection) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel == null) {
            return new Projection(level, new Vec3(localPosition.x, localPosition.y, localPosition.z), new Vec3(localDirection.x, localDirection.y, localDirection.z));
        }

        final Vector3d worldPos = new Vector3d(localPosition);
        final Vector3d worldDir = new Vector3d(localDirection);
        subLevel.logicalPose().transformPosition(worldPos);
        subLevel.logicalPose().transformNormal(worldDir);

        return new Projection(subLevel.getLevel(), new Vec3(worldPos.x, worldPos.y, worldPos.z), new Vec3(worldDir.x, worldDir.y, worldDir.z));
    }

    public record Projection(Level level, Vec3 position, Vec3 direction) {
    }
}
