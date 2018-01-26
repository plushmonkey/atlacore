package com.plushnode.atlacore.util;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.platform.User;

import java.util.HashMap;
import java.util.Map;

// This is a reference counting object that's used to manage a user's flight.
// Every time a reference is acquired, it should eventually be released.
// If the reference count drops to 0 then the user will lose flight.
// Note: Actual flight and just fall damage prevention might need to be separated into two distinct things.
// That would make it easier to take away Flight without removing fall damage protection at the same time.
public class Flight {
    private static Map<User, Flight> instances = new HashMap<>();

    private User user;
    private int references;
    private boolean couldFly;
    private boolean wasFlying;
    private boolean isFlying;

    private Flight(User user) {
        this.user = user;
        this.references = 0;
        this.isFlying = false;

        couldFly = user.getAllowFlight();
        wasFlying = user.isFlying();

        instances.put(user, this);
    }

    // Returns the Flight instance for a user. This will increment the flight counter.
    // Call release() to decrement the counter.
    // Call remove() to completely remove flight.
    public static Flight get(User user) {
        Flight flight = instances.get(user);
        if (flight == null) {
            flight = new Flight(user);

            instances.put(user, flight);
            System.out.println("Enabling flight for " + user);
        }

        flight.references++;
        System.out.println("Flight references: " + flight.references + " for " + user);
        return flight;
    }

    public static boolean hasFlight(User user) {
        return instances.containsKey(user);
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
        user.setFlying(flying);
        user.setAllowFlight(flying);
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    // Decrements the user's flight counter. If this goes below 1 then the user loses flight.
    public Flight release() {
        if (--references <= 0) {
            remove(user);
            return null;
        }
        return this;
    }

    // Completely releases flight for the user.
    // This will set the user back to the state before any Flight was originally added.
    public static void remove(User user) {
        Flight flight = instances.get(user);

        System.out.println("Removing flight for " + user);
        if (flight != null) {
            user.setFlying(flight.wasFlying);
            user.setAllowFlight(flight.couldFly);
        }

        instances.remove(user);
    }

    public static void removeAll() {
        for (Map.Entry<User, Flight> entry : instances.entrySet()) {
            User user = entry.getKey();
            Flight flight = entry.getValue();

            user.setFlying(flight.wasFlying);
            user.setAllowFlight(flight.couldFly);
        }

        instances.clear();
    }

    public static void updateAll() {
        for (Map.Entry<User, Flight> entry : instances.entrySet()) {
            User user = entry.getKey();
            Flight flight = entry.getValue();

            user.setFlying(flight.isFlying);
        }
    }

    // This class will apply flight when constructed and then remove it when the player touches the ground.
    public static class GroundRemovalTask implements Task {
        private Flight flight;
        private User user;
        private Task task;
        private long start;
        private long maxDuration;

        public GroundRemovalTask(User user, int initialDelay) {
            this(user, initialDelay, 10000L);
        }

        public GroundRemovalTask(User user, int initialDelay, long maxDuration) {
            this.user = user;
            this.flight = Flight.get(user);
            this.start = System.currentTimeMillis();
            this.maxDuration = maxDuration;

            task = Game.plugin.createTaskTimer(this, initialDelay, 1);
        }

        @Override
        public void run() {
            long time = System.currentTimeMillis();

            if (time >= start + maxDuration || user.isOnGround()) {
                // Remove flight next tick so Flight still exists during the fall event handler.
                Game.plugin.createTask(() -> {
                    if (flight != null) {
                        flight.release();
                        task.cancel();

                        flight = null;
                    }
                }, 1);
            }
        }
    }
}
