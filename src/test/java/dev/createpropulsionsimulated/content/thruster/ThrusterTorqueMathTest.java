package dev.createpropulsionsimulated.content.thruster;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThrusterTorqueMathTest {

    @Test
    void centeredThrusterProducesNoTorque() {
        final Vector3d com = new Vector3d(0.0, 0.0, 0.0);
        final List<ThrusterTorqueMath.ForceAtPoint> forces = List.of(
                new ThrusterTorqueMath.ForceAtPoint(new Vector3d(0.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 12.0))
        );

        final Vector3d torque = ThrusterTorqueMath.netTorqueAbout(com, forces);
        assertEquals(0.0, torque.length(), 1.0e-9);
    }

    @Test
    void offCenterThrusterProducesTorque() {
        final Vector3d com = new Vector3d(0.0, 0.0, 0.0);
        final List<ThrusterTorqueMath.ForceAtPoint> forces = List.of(
                new ThrusterTorqueMath.ForceAtPoint(new Vector3d(2.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 10.0))
        );

        final Vector3d torque = ThrusterTorqueMath.netTorqueAbout(com, forces);
        assertEquals(20.0, Math.abs(torque.y), 1.0e-9);
        assertEquals(0.0, torque.x, 1.0e-9);
        assertEquals(0.0, torque.z, 1.0e-9);
    }

    @Test
    void symmetricMultipleThrustersCancelTorqueAndSumForce() {
        final Vector3d com = new Vector3d(0.0, 0.0, 0.0);
        final List<ThrusterTorqueMath.ForceAtPoint> forces = List.of(
                new ThrusterTorqueMath.ForceAtPoint(new Vector3d(2.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 10.0)),
                new ThrusterTorqueMath.ForceAtPoint(new Vector3d(-2.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 10.0))
        );

        final Vector3d torque = ThrusterTorqueMath.netTorqueAbout(com, forces);
        final Vector3d force = ThrusterTorqueMath.netForce(forces);

        assertEquals(0.0, torque.length(), 1.0e-9);
        assertEquals(20.0, force.z, 1.0e-9);
        assertEquals(0.0, force.x, 1.0e-9);
        assertEquals(0.0, force.y, 1.0e-9);
    }
}
