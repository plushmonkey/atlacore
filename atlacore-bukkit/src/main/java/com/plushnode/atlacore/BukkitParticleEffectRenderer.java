package com.plushnode.atlacore;

import com.plushnode.atlacore.util.ReflectionUtil;
import com.plushnode.atlacore.wrappers.LocationWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BukkitParticleEffectRenderer implements ParticleEffectRenderer {
    static {
        ParticlePacket.initialize();
    }

    @Override
    public void display(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        if (!isSupported(effect)) {
            return;
        }

        if (effect.getRequiresData()) {
            return;
        }

        org.bukkit.Location bukkitCenter = ((LocationWrapper)center).getBukkitLocation();

        if (effect.getRequiresWater() && !isWater(bukkitCenter)) {
            throw new IllegalArgumentException("There is no water at the center location");
        }

        new ParticlePacket(effect, offsetX, offsetY, offsetZ, speed, amount, range > 256, null).sendTo(bukkitCenter, range);
    }

    private static boolean isSupported(ParticleEffect effect) {
        if (effect.getRequiredVersion() == -1) {
            return true;
        }

        return ParticlePacket.getVersion() >= effect.getRequiredVersion();
    }

    private static boolean isWater(org.bukkit.Location location) {
        Material type = location.getBlock().getType();

        return type == Material.WATER || type == Material.STATIONARY_WATER;
    }

    public static final class ParticlePacket {
        private static int version;
        private static Class<?> enumParticle;
        private static Constructor<?> packetConstructor;
        private static Method getHandle;
        private static Field playerConnection;
        private static Method sendPacket;
        private static boolean initialized;
        private final ParticleEffect effect;
        private final float offsetX;
        private final float offsetY;
        private final float offsetZ;
        private final float speed;
        private final int amount;
        private final boolean longDistance;
        private final ParticleEffect.ParticleData data;
        private Object packet;

        /**
         * Construct a new particle packet
         *
         * @param effect Particle effect
         * @param offsetX Maximum distance particles can fly away from the
         *            center on the x-axis
         * @param offsetY Maximum distance particles can fly away from the
         *            center on the y-axis
         * @param offsetZ Maximum distance particles can fly away from the
         *            center on the z-axis
         * @param speed Display speed of the particles
         * @param amount Amount of particles
         * @param longDistance Indicates whether the maximum distance is
         *            increased from 256 to 65536
         * @param data Data of the effect
         * @throws IllegalArgumentException If the speed is lower than 0 or the
         *             amount is lower than 1
         * @see #initialize()
         */
        public ParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException {
            initialize();
            if (speed < 0) {
                throw new IllegalArgumentException("The speed is lower than 0");
            }
            this.effect = effect;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.amount = amount;
            this.longDistance = longDistance;
            this.data = data;
        }

        /**
         * Construct a new particle packet of a single particle flying into a
         * determined direction
         *
         * @param effect Particle effect
         * @param direction Direction of the particle
         * @param speed Display speed of the particle
         * @param longDistance Indicates whether the maximum distance is
         *            increased from 256 to 65536
         * @param data Data of the effect
         * @throws IllegalArgumentException If the speed is lower than 0
         * @see #initialize()
         */
        public ParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException {
            initialize();
            if (speed < 0) {
                throw new IllegalArgumentException("The speed is lower than 0");
            }
            this.effect = effect;
            this.offsetX = (float) direction.getX();
            this.offsetY = (float) direction.getY();
            this.offsetZ = (float) direction.getZ();
            this.speed = speed;
            this.amount = 0;
            this.longDistance = longDistance;
            this.data = data;
        }

        /**
         * Initializes {@link #packetConstructor}, {@link #getHandle},
         * {@link #playerConnection} and {@link #sendPacket} and sets
         * {@link #initialized} to <code>true</code> if it succeeds
         * <p>
         * <b>Note:</b> These fields only have to be initialized once, so it
         * will return if {@link #initialized} is already set to
         * <code>true</code>
         *
         * @throws VersionIncompatibleException if your bukkit version is not
         *             supported by this library
         */
        public static void initialize() throws VersionIncompatibleException {
            if (initialized) {
                return;
            }
            try {
                version = Integer.parseInt(ReflectionUtil.PackageType.getServerVersion().split("_")[1]);
                if (version > 7) {
                    enumParticle = ReflectionUtil.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
                }
                Class<?> packetClass = ReflectionUtil.PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
                packetConstructor = ReflectionUtil.getConstructor(packetClass);
                getHandle = ReflectionUtil.getMethod("CraftPlayer", ReflectionUtil.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
                playerConnection = ReflectionUtil.getField("EntityPlayer", ReflectionUtil.PackageType.MINECRAFT_SERVER, false, "playerConnection");
                sendPacket = ReflectionUtil.getMethod(playerConnection.getType(), "sendPacket", ReflectionUtil.PackageType.MINECRAFT_SERVER.getClass("Packet"));
            }
            catch (Exception exception) {
                throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
            }
            initialized = true;
        }

        /**
         * Returns the version of your server (1.x)
         *
         * @return The version number
         */
        public static int getVersion() {
            return version;
        }

        /**
         * Determine if {@link #packetConstructor}, {@link #getHandle},
         * {@link #playerConnection} and {@link #sendPacket} are initialized
         *
         * @return Whether these fields are initialized or not
         * @see #initialize()
         */
        public static boolean isInitialized() {
            return initialized;
        }

        /**
         * Sends the packet to a single player and caches it
         *
         * @param center Center location of the effect
         * @param player Receiver of the packet
         * @throws PacketInstantiationException if instantion fails due to an
         *             unknown error
         * @throws PacketSendingException if sending fails due to an unknown
         *             error
         */
        public void sendTo(org.bukkit.Location center, org.bukkit.entity.Player player) throws PacketInstantiationException, PacketSendingException {
            if (packet == null) {
                try {
                    packet = packetConstructor.newInstance();
                    Object id;
                    if (version < 8) {
                        id = effect.getName();
                        if (data != null) {
                            id += data.getPacketDataString();
                        }
                    } else {
                        id = enumParticle.getEnumConstants()[effect.getId()];
                    }
                    ReflectionUtil.setValue(packet, true, "a", id);
                    ReflectionUtil.setValue(packet, true, "b", (float) center.getX());
                    ReflectionUtil.setValue(packet, true, "c", (float) center.getY());
                    ReflectionUtil.setValue(packet, true, "d", (float) center.getZ());
                    ReflectionUtil.setValue(packet, true, "e", offsetX);
                    ReflectionUtil.setValue(packet, true, "f", offsetY);
                    ReflectionUtil.setValue(packet, true, "g", offsetZ);
                    ReflectionUtil.setValue(packet, true, "h", speed);
                    ReflectionUtil.setValue(packet, true, "i", amount);
                    if (version > 7) {
                        ReflectionUtil.setValue(packet, true, "j", longDistance);
                        ReflectionUtil.setValue(packet, true, "k", data == null ? new int[0] : data.getPacketData());
                    }
                }
                catch (Exception exception) {
                    throw new PacketInstantiationException("Packet instantiation failed", exception);
                }
            }
            try {
                sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
            }
            catch (Exception exception) {
                throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
            }
        }

        /**
         * Sends the packet to all players in the list
         *
         * @param center Center location of the effect
         * @param players Receivers of the packet
         * @throws IllegalArgumentException If the player list is empty
         * @see #sendTo(Location center, Player player)
         */
        public void sendTo(org.bukkit.Location center, List<org.bukkit.entity.Player> players) throws IllegalArgumentException {
            if (players.isEmpty()) {
                throw new IllegalArgumentException("The player list is empty");
            }
            for (org.bukkit.entity.Player player : players) {
                sendTo(center, player);
            }
        }

        /**
         * Sends the packet to all players in a certain range
         *
         * @param center Center location of the effect
         * @param range Range in which players will receive the packet (Maximum
         *            range for particles is usually 16, but it can differ for
         *            some types)
         * @throws IllegalArgumentException If the range is lower than 1
         * @see #sendTo(Location center, Player player)
         */
        public void sendTo(org.bukkit.Location center, double range) throws IllegalArgumentException {
            if (range < 1) {
                throw new IllegalArgumentException("The range is lower than 1");
            }

            double squared = range * range;
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() != center.getWorld() || player.getLocation().distanceSquared(center) > squared) {
                    continue;
                }
                sendTo(center, player);
            }
        }

        /**
         * Represents a runtime exception that is thrown if a bukkit version is
         * not compatible with this library
         * <p>
         * This class is part of the <b>ParticleEffect Library</b> and follows
         * the same usage conditions
         *
         * @author DarkBlade12
         * @since 1.5
         */
        private static final class VersionIncompatibleException extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            /**
             * Construct a new version incompatible exception
             *
             * @param message Message that will be logged
             * @param cause Cause of the exception
             */
            public VersionIncompatibleException(String message, Throwable cause) {
                super(message, cause);
            }
        }

        /**
         * Represents a runtime exception that is thrown if packet instantiation
         * fails
         * <p>
         * This class is part of the <b>ParticleEffect Library</b> and follows
         * the same usage conditions
         *
         * @author DarkBlade12
         * @since 1.4
         */
        private static final class PacketInstantiationException extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            /**
             * Construct a new packet instantiation exception
             *
             * @param message Message that will be logged
             * @param cause Cause of the exception
             */
            public PacketInstantiationException(String message, Throwable cause) {
                super(message, cause);
            }
        }

        /**
         * Represents a runtime exception that is thrown if packet sending fails
         * <p>
         * This class is part of the <b>ParticleEffect Library</b> and follows
         * the same usage conditions
         *
         * @author DarkBlade12
         * @since 1.4
         */
        private static final class PacketSendingException extends RuntimeException {
            private static final long serialVersionUID = 3203085387160737484L;

            /**
             * Construct a new packet sending exception
             *
             * @param message Message that will be logged
             * @param cause Cause of the exception
             */
            public PacketSendingException(String message, Throwable cause) {
                super(message, cause);
            }
        }
    }
}
