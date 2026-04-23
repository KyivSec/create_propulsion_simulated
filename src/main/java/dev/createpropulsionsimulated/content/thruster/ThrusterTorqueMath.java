package dev.createpropulsionsimulated.content.thruster;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public final class ThrusterTorqueMath {
    private ThrusterTorqueMath() {
    }

    public static Vector3d netForce(final List<ForceAtPoint> forces) {
        final Vector3d result = new Vector3d();
        for (final ForceAtPoint force : forces) {
            result.add(force.force());
        }
        return result;
    }

    public static Vector3d netTorqueAbout(final Vector3dc centerOfMass, final List<ForceAtPoint> forces) {
        final Vector3d result = new Vector3d();
        final Vector3d arm = new Vector3d();
        final Vector3d torque = new Vector3d();
        for (final ForceAtPoint force : forces) {
            arm.set(force.point()).sub(centerOfMass);
            arm.cross(force.force(), torque);
            result.add(torque);
        }
        return result;
    }

    public record ForceAtPoint(Vector3dc point, Vector3dc force) {
    }
}
