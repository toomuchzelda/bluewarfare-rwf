package me.libraryaddict.core.utils;

import me.libraryaddict.core.data.ParticleColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Collection;

public class UtilParticle {
    public static void playParticle(Location location, ParticleColor color) {
        playParticle(location, color, ViewDist.NORMAL, UtilPlayer.getPlayers().toArray(new Player[0]));
    }

    public static void playParticle(Location location, ParticleColor color, Player... players) {
        playParticle(location, color, ViewDist.NORMAL, players);
    }

    public static void playParticle(Location location, ParticleColor color, ViewDist viewDist) {
        playParticle(Particle.REDSTONE, new Particle.DustOptions(color.color, 1), location, 0, 0, 0, 1, 0, viewDist, UtilPlayer.getPlayers().toArray(new Player[0]));
    }

    public static void playParticle(Location location, ParticleColor color, ViewDist viewDist, Player... players) {
        playParticle(Particle.REDSTONE, new Particle.DustOptions(color.color, 1), location, 0, 0, 0, 1, 0, viewDist, players);
    }

    public static void playParticle(Particle type, Location location) {
        playParticle(type, location, 1);
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ) {
        playParticle(type, location, offsetX, offsetY, offsetZ, 1, ViewDist.NORMAL);
    }

    public static void playParticle(Particle particleType, Location loc, double offsetX, double offsetY,
                                    double offsetZ, double speed, int count) {
        playParticle(particleType, loc, offsetX, offsetY, offsetZ, speed, count, ViewDist.NORMAL);
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ, double speed, int count, Player... players) {
        playParticle(type, location, offsetX, offsetY, offsetZ, speed, count, ViewDist.NORMAL, players);
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ, double speed, int count, ViewDist dist) {
        playParticle(type, location, offsetX, offsetY, offsetZ, speed, count, dist, UtilPlayer.getPlayers());
    }

    public static void playParticle(Particle particle, Location location, double offsetX, double offsetY,
                                    double offsetZ, double speed, int count, ViewDist dist, Collection<Player> players) {
        playParticle(particle, location, offsetX, offsetY, offsetZ, speed, count, dist, players.toArray(new Player[0]));
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ, double speed, int count, ViewDist dist, Player... players) {
        playParticle(type, null, location, offsetX, offsetY, offsetZ, speed, count, dist, players);
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ, int count, ViewDist dist) {
        playParticle(type, location, offsetX, offsetY, offsetZ, 0, count, dist);
    }

    public static void playParticle(Particle type, Location location, double offsetX, double offsetY,
                                    double offsetZ, int count, ViewDist dist, Player... players) {
        playParticle(type, location, offsetX, offsetY, offsetZ, 0, count, dist, players);
    }

    public static void playParticle(Particle type, Location location, int count) {
        playParticle(type, location, count, ViewDist.NORMAL);
    }

    public static void playParticle(Particle type, Location location, Player... players) {
        playParticle(type, location, 0F, 0F, 0F, 1, ViewDist.NORMAL, players);
    }

    public static void playParticle(Particle type, Location location, int count, ViewDist viewDist) {
        playParticle(type, location, 0F, 0F, 0F, count, viewDist);
    }

    public static void playParticle(Particle particleType, Location location, ViewDist viewDist) {
        UtilParticle.playParticle(particleType, location, 1, viewDist);
    }

    public static <T> void playParticle(Particle particle, T data, Location location, double offsetX, double offsetY, double offsetZ,
                                    double speed, int count, ViewDist dist, Player... players) {
        if (players.length == 0)
            players = UtilPlayer.getPlayers().toArray(new Player[0]);
        for (Player player : players) {
            if (player.getWorld() != location.getWorld())
                continue;

            // Out of range for player
            if (UtilLoc.getDistance(player.getLocation(), location) > dist.getDist())
                continue;

            player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
        }
    }


    public enum ViewDist {
        LONG(48),
        LONGER(96),
        MAX(256),
        NORMAL(24),
        SHORT(8);

        private int _dist;

        ViewDist(int dist) {
            _dist = dist;
        }

        public int getDist() {
            return _dist;
        }
    }
}