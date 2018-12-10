package com.plushnode.atlacore.math;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

// Generates a spline, using cubic hermite curves, from a set of knots.
// Set tension to 0 to generate a Catmull-Rom spline.
public class CubicHermiteSpline {
    private List<Vector3D> knots = new ArrayList<>();
    private List<CubicHermiteCurve> curves = new ArrayList<>();
    private double length;
    private boolean built;
    private double tension;

    public CubicHermiteSpline() {
        this(0.0);
    }

    public CubicHermiteSpline(double tension) {
        this.tension = tension;
    }

    public void addKnot(Vector3D position) {
        knots.add(position);
        built = false;
    }

    public List<Vector3D> getKnots() {
        return knots;
    }

    public Vector3D interpolate(double t) {
        t = Math.max(0.0, Math.min(t, 1.0));

        if (!built) {
            build();
        }

        double tStart = 0.0;

        for (int i = 0; i < curves.size(); ++i) {
            CubicHermiteCurve curve = curves.get(i);

            double tEnd = tStart + Math.max(curve.getLength(), 0.000001) / length;

            if (tStart <= t && t <= tEnd) {
                double curveT = (t - tStart) / (tEnd - tStart);
                return curve.interpolate(curveT);
            }

            tStart += (tEnd - tStart);
        }

        return curves.get(curves.size() - 1).interpolate(1.0);
    }

    public void build() {
        Vector3D startPoint, startTangent, endPoint, endTangent;

        length = 0.0;
        curves.clear();

        for (int i = 0; i < knots.size() - 1; ++i) {
            startPoint = knots.get(i);
            endPoint = knots.get(i + 1);

            // Compute tangents
            if (i > 0) {
                startTangent = endPoint.subtract(knots.get(i - 1)).scalarMultiply(0.5);
            } else {
                startTangent = endPoint.subtract(startPoint);
            }

            if (i < knots.size() - 2) {
                endTangent = knots.get(i + 2).subtract(startPoint).scalarMultiply(0.5);
            } else {
                endTangent = endPoint.subtract(startPoint);
            }

            // Apply tension to the tangents
            startTangent = startTangent.scalarMultiply(1.0 - this.tension);
            endTangent = endTangent.scalarMultiply(1.0 - this.tension);
            CubicHermiteCurve curve = new CubicHermiteCurve(startPoint, startTangent, endPoint, endTangent);

            length += curve.getLength();
            curves.add(curve);
        }

        built = true;
    }

    public double getLength() {
        return length;
    }
}
