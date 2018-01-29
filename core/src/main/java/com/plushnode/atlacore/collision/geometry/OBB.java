package com.plushnode.atlacore.collision.geometry;

import com.plushnode.atlacore.collision.Collider;
import com.plushnode.atlacore.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

// Oriented bounding box
public class OBB implements Collider {
    private Vector3D center;
    private RealMatrix basis;
    // Half extents in local space.
    private Vector3D e;

    public OBB(Vector3D center, Vector3D halfExtents, Vector3D axis0, Vector3D axis1, Vector3D axis2) {
        this.center = center;
        this.e = halfExtents;
        this.basis = MatrixUtils.createRealMatrix(3, 3);
        this.basis.setRow(0, axis0.toArray());
        this.basis.setRow(1, axis1.toArray());
        this.basis.setRow(2, axis2.toArray());
    }

    public OBB(AABB aabb) {
        this.center = aabb.getPosition();
        this.basis = MatrixUtils.createRealIdentityMatrix(3);
        this.e = aabb.getHalfExtents();
    }

    public OBB(AABB aabb, Rotation rotation) {
        this.center = aabb.getPosition();
        this.basis = MatrixUtils.createRealMatrix(rotation.getMatrix());
        this.e = aabb.getHalfExtents();
    }

    @Override
    public boolean intersects(Collider collider) {
        if (collider instanceof Sphere) {
            return ((Sphere)collider).intersects(this);
        } else if (collider instanceof AABB) {
            return intersects(new OBB((AABB)collider));
        } else if (collider instanceof OBB) {
            return intersects((OBB)collider);
        }

        return false;
    }

    public boolean intersects(OBB other) {
        final double epsilon = 0.000001;
        double ra, rb;

        RealMatrix R = getRotationMatrix(other);
        // translation
        Vector3D t = other.center.subtract(center);
        // Bring into coordinate frame
        t = new Vector3D(R.operate(t.toArray()));
        RealMatrix absR = MatrixUtils.createRealMatrix(3, 3);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                absR.setEntry(i, j, Math.abs(R.getEntry(i, j)) + epsilon);
            }
        }

        // test this box's axes
        for (int i = 0; i < 3; ++i) {
            Vector3D row = new Vector3D(absR.getRow(i));

            ra = VectorUtil.getEntry(e, i);
            rb = other.e.dotProduct(row);

            if (Math.abs(VectorUtil.getEntry(t, i)) > ra + rb) {
                return false;
            }
        }

        // test other box's axes
        for (int i = 0; i < 3; ++i) {
            Vector3D col = new Vector3D(absR.getColumn(i));

            ra = e.dotProduct(col);
            rb = VectorUtil.getEntry(other.e, i);

            Vector3D rotCol = new Vector3D(R.getColumn(i));
            if (Math.abs(t.dotProduct(rotCol)) > ra + rb) {
                return false;
            }
        }

        // A0 x B0
        ra = VectorUtil.getEntry(e, 1) * absR.getEntry(2, 0) + VectorUtil.getEntry(e, 2) * absR.getEntry(1, 0);
        rb = VectorUtil.getEntry(other.e, 1) * absR.getEntry(0, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(0, 1);
        if (Math.abs(VectorUtil.getEntry(t, 2) * R.getEntry(1, 0) - VectorUtil.getEntry(t, 1) * R.getEntry(2, 0)) > ra + rb) {
            return false;
        }

        // A0 x B1
        ra = VectorUtil.getEntry(e, 1) * absR.getEntry(2, 1) + VectorUtil.getEntry(e, 2) * absR.getEntry(1, 1);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(0, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(0, 0);
        if (Math.abs(VectorUtil.getEntry(t, 2) * R.getEntry(1, 1) - VectorUtil.getEntry(t, 1) * R.getEntry(2, 1)) > ra + rb) {
            return false;
        }

        // A0 x B2
        ra = VectorUtil.getEntry(e, 1) * absR.getEntry(2, 2) + VectorUtil.getEntry(e, 2) * absR.getEntry(1, 2);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(0, 1) + VectorUtil.getEntry(other.e, 1) * absR.getEntry(0, 0);
        if (Math.abs(VectorUtil.getEntry(t, 2) * R.getEntry(1, 2) - VectorUtil.getEntry(t, 1) * R.getEntry(2, 2)) > ra + rb) {
            return false;
        }

        // A1 x B0
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(2, 0) + VectorUtil.getEntry(e, 2) * absR.getEntry(0, 0);
        rb = VectorUtil.getEntry(other.e, 1) * absR.getEntry(1, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(1, 1);
        if (Math.abs(VectorUtil.getEntry(t, 0) * R.getEntry(2, 0) - VectorUtil.getEntry(t, 2) * R.getEntry(0, 0)) > ra + rb) {
            return false;
        }

        // A1 x B1
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(2, 1) + VectorUtil.getEntry(e, 2) * absR.getEntry(0, 1);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(1, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(1, 0);
        if (Math.abs(VectorUtil.getEntry(t, 0) * R.getEntry(2, 1) - VectorUtil.getEntry(t, 2) * R.getEntry(0, 1)) > ra + rb) {
            return false;
        }

        // A1 x B2
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(2, 2) + VectorUtil.getEntry(e, 2) * absR.getEntry(0, 2);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(1, 1) + VectorUtil.getEntry(other.e, 1) * absR.getEntry(1, 0);
        if (Math.abs(VectorUtil.getEntry(t, 0) * R.getEntry(2, 2) - VectorUtil.getEntry(t, 2) * R.getEntry(0, 2)) > ra + rb) {
            return false;
        }

        // A2 x B0
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(1, 0) + VectorUtil.getEntry(e, 1) * absR.getEntry(0, 0);
        rb = VectorUtil.getEntry(other.e, 1) * absR.getEntry(2, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(2, 1);
        if (Math.abs(VectorUtil.getEntry(t, 1) * R.getEntry(0, 0) - VectorUtil.getEntry(t, 0) * R.getEntry(1, 0)) > ra + rb) {
            return false;
        }

        // A2 x B1
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(1, 1) + VectorUtil.getEntry(e, 1) * absR.getEntry(0, 1);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(2, 2) + VectorUtil.getEntry(other.e, 2) * absR.getEntry(2, 0);
        if (Math.abs(VectorUtil.getEntry(t, 1) * R.getEntry(0, 1) - VectorUtil.getEntry(t, 0) * R.getEntry(1, 1)) > ra + rb) {
            return false;
        }

        // A2 x B2
        ra = VectorUtil.getEntry(e, 0) * absR.getEntry(1, 2) + VectorUtil.getEntry(e, 1) * absR.getEntry(0, 2);
        rb = VectorUtil.getEntry(other.e, 0) * absR.getEntry(2, 1) + VectorUtil.getEntry(other.e, 1) * absR.getEntry(2, 0);
        if (Math.abs(VectorUtil.getEntry(t, 1) * R.getEntry(0, 2) - VectorUtil.getEntry(t, 0) * R.getEntry(1, 2)) > ra + rb) {
            return false;
        }

        return true;
    }

    // Express the other box's basis in this box's coordinate frame.
    private RealMatrix getRotationMatrix(OBB other) {
        RealMatrix r = MatrixUtils.createRealMatrix(3, 3);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Vector3D a = new Vector3D(basis.getRow(i));
                Vector3D b = new Vector3D(other.basis.getRow(j));

                r.setEntry(i, j, a.dotProduct(b));
            }
        }

        return r;
    }

    // Returns the position closest to the target that lies on/in the OBB.
    public Vector3D getClosestPosition(Vector3D target) {
        Vector3D t = target.subtract(center);
        Vector3D closest = center;

        // Project target onto basis axes and move toward it.
        for (int i = 0; i < 3; ++i) {
            Vector3D axis = new Vector3D(basis.getRow(i));
            double r = VectorUtil.getEntry(e, i);
            double dist = Math.max(-r, Math.min(t.dotProduct(axis), r));

            closest = closest.add(axis.scalarMultiply(dist));
        }

        return closest;
    }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public Vector3D getHalfExtents() {
        double x = e.dotProduct(Vector3D.PLUS_I);
        double y = e.dotProduct(Vector3D.PLUS_J);
        double z = e.dotProduct(Vector3D.PLUS_K);

        return new Vector3D(x, y, z);
    }

    @Override
    public boolean contains(Vector3D point) {
        double epsilon = 0.001;
        return getClosestPosition(point).distanceSq(point) <= epsilon;
    }

    public Vector3D getHalfDiagonal() {
        Vector3D result = Vector3D.ZERO;

        for (int i = 0; i < 3; ++i) {
            result = result.add(new Vector3D(basis.getRow(i)).scalarMultiply(VectorUtil.getEntry(e, i)));
        }

        return result;
    }
}
