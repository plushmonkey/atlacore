package com.plushnode.atlacore.util;

import com.plushnode.atlacore.config.Configurable;
import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.Entity;

import java.util.HashMap;
import java.util.Map;

public final class FireTick {
    public static Config config = new Config();

    private static Map<String, FireTickMethod> methods = new HashMap<>();
    private static FireTickMethod method = new OverwriteFireTickMethod();

    static {
        methods.put("overwrite", new OverwriteFireTickMethod());
        methods.put("larger", new LargerFireTickMethod());
        methods.put("accumulate", new AccumulateFireTickMethod());
    }

    public static void set(Entity target, int amount) {
        method.set(target, amount);
    }

    private interface FireTickMethod {
        void set(Entity entity, int amount);
    }

    // Always set the target's fire tick to the new amount.
    private static class OverwriteFireTickMethod implements FireTickMethod {
        @Override
        public void set(Entity entity, int amount) {
            entity.setFireTicks(amount);
        }
    }

    // Only overwrite target's fire tick if the new fire tick is larger.
    private static class LargerFireTickMethod implements FireTickMethod {
        @Override
        public void set(Entity entity, int amount) {
            if (entity.getFireTicks() < amount) {
                entity.setFireTicks(amount);
            }
        }
    }

    // Increase the target's fire tick instead of overwriting.
    private static class AccumulateFireTickMethod implements FireTickMethod {
        @Override
        public void set(Entity entity, int amount) {
            entity.setFireTicks(entity.getFireTicks() + amount);
        }
    }

    private static class Config extends Configurable {
        @Override
        public void onConfigReload() {
            String methodName = config.getNode("properties", "fire-tick-method").getString("larger");

            if (methods == null) {
                method = new LargerFireTickMethod();
                return;
            }

            FireTickMethod newMethod = methods.get(methodName.toLowerCase());
            if (newMethod != null) {
                Game.info("Using " + methodName + " as FireTickMethod.");
                method = newMethod;
            } else {
                Game.warn(methodName + " not a known FireTickMethod. Defaulting to larger method.");
                method = new LargerFireTickMethod();
            }
        }
    }
}
