package com.plushnode.atlacore.game.ability.common;

import com.plushnode.atlacore.game.ability.Ability;
import com.plushnode.atlacore.game.ability.UpdateResult;
import com.plushnode.atlacore.platform.User;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BurstAbility implements Ability {
    protected List<Burstable> blasts = new ArrayList<>();

    protected void createBurst(User user, double thetaMin, double thetaMax, double thetaStep, double phiMin, double phiMax, double phiStep, Class<? extends Burstable> type) {
        for (double theta = thetaMin; theta < thetaMax; theta += thetaStep) {
            for (double phi = phiMin; phi < phiMax; phi += phiStep) {
                double x = Math.cos(phi) * Math.sin(theta);
                double y = Math.cos(phi) * Math.cos(theta);
                double z = Math.sin(phi);

                Vector3D direction = new Vector3D(x, y, z);

                Burstable blast;
                try {
                    blast = type.newInstance();
                } catch (InstantiationException|IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }

                blast.initialize(user, user.getEyeLocation().add(direction), direction);
                blasts.add(blast);
            }
        }
    }

    protected void createCone(User user, Class<? extends Burstable> type) {
        for (double theta = 0.0; theta < Math.PI; theta += Math.toRadians(10)) {
            for (double phi = 0.0; phi < Math.PI * 2; phi += Math.toRadians(10)) {
                double x = Math.cos(phi) * Math.sin(theta);
                double y = Math.cos(phi) * Math.cos(theta);
                double z = Math.sin(phi);

                Vector3D direction = new Vector3D(x, y, z);

                if (Vector3D.angle(direction, user.getDirection()) > Math.toRadians(30)) {
                    continue;
                }

                Burstable blast;
                try {
                    blast = type.newInstance();
                } catch (InstantiationException|IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }

                blast.initialize(user, user.getEyeLocation().add(direction), direction);
                blasts.add(blast);
            }
        }
    }

    // Return false if all blasts are finished.
    protected boolean updateBurst() {
        for (Iterator<Burstable> iterator = blasts.iterator(); iterator.hasNext();) {
            Burstable blast = iterator.next();

            if (blast.update() != UpdateResult.Continue) {
                iterator.remove();
            }
        }

        return !blasts.isEmpty();
    }

    protected void setRenderInterval(long interval) {
        for (Burstable blast : blasts) {
            blast.setRenderInterval(interval);
        }
    }

    protected void setRenderParticleCount(int count) {
        for (Burstable blast : blasts) {
            blast.setRenderParticleCount(count);
        }
    }
}
