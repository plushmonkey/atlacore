package com.plushnode.atlacore.math;

import com.plushnode.atlacore.platform.Location;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class LineSegment {
    public Vector3D start;
    public Vector3D end;

    public LineSegment(Vector3D start, Vector3D end) {
        this.start = start;
        this.end = end;
    }

    public LineSegment(Location start, Location end) {
        this.start = start.toVector();
        this.end = end.toVector();
    }

    public Vector3D getClosestPoint(Vector3D target) {
        Vector3D toEnd = end.subtract(start);
        double t = target.subtract(start).dotProduct(toEnd) / toEnd.dotProduct(toEnd);
        // Cap t to the ends
        t = Math.max(0.0, Math.min(t, 1.0));
        return start.add(toEnd.scalarMultiply(t));
    }

    public Vector3D getDirection() {
        return end.subtract(start).normalize();
    }

    public Vector3D interpolate(double t) {
        t = Math.max(0.0, Math.min(t, 1.0));
        return start.add(end.subtract(start).scalarMultiply(t));
    }
}
