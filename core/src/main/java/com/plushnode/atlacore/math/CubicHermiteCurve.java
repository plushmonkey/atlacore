package com.plushnode.atlacore.math;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

// Represents a curve between a start and end point.
public class CubicHermiteCurve {
    private static final int LENGTH_INTERPOLATION_COUNT = 5;

    private Vector3D startPoint;
    private Vector3D startTangent;
    private Vector3D endPoint;
    private Vector3D endTangent;
    private double chordalDistance;

    public CubicHermiteCurve(Vector3D startPoint, Vector3D startTanget, Vector3D endPoint, Vector3D endTangent) {
        this.startPoint = startPoint;
        this.startTangent = startTanget;
        this.endPoint = endPoint;
        this.endTangent = endTangent;
        // Approximate arc-length by just using chordal distance.
        this.chordalDistance = endPoint.distance(startPoint);
    }

    public Vector3D interpolate(double t) {
        t = Math.max(0.0, Math.min(t, 1.0));

        double startPointT = (2.0 * t * t * t) - (3.0 * t * t) + 1.0;
        double startTangentT = (t * t * t) - (2.0 * t * t) + t;
        double endPointT = (-2.0 * t * t * t) + (3.0 * t * t);
        double endTangentT = (t * t * t) - (t * t);

        return startPoint.scalarMultiply(startPointT)
                .add(startTangent.scalarMultiply(startTangentT))
                .add(endPoint.scalarMultiply(endPointT))
                .add(endTangent.scalarMultiply(endTangentT));
    }

    // Approximate length by summing length of n interpolated lines.
    public double getLength() {
        double result = 0.0;

        Vector3D current = startPoint;

        for (int i = 0; i < LENGTH_INTERPOLATION_COUNT; ++i) {
            Vector3D interpolated = interpolate(i / (double)LENGTH_INTERPOLATION_COUNT);

            result += current.distance(interpolated);
            current = interpolated;
        }

        return result;
    }
}
