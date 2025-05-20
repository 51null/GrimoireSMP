package noah.grimoireSMP.scrollmanagers.temporal;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.TemporalScroll;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TemporalHistoryManager {
    // Keep 10 snapshots (one per second)
    private static final int MAX_SNAPSHOTS = 11;
    private static final Map<UUID, Deque<Snapshot>> history = new ConcurrentHashMap<>();

    /** Call once from onEnable() to start the snapshots. */
    public static void start(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    PlayerScrollData data = PlayerData.getPlayerScrollData(p);
                    if (data == null) continue;
                    if (!(data.getScroll() instanceof TemporalScroll)) continue;
                    if (data.getScroll().isLost()) continue;

                    UUID u = p.getUniqueId();
                    history.computeIfAbsent(u, __ -> new ArrayDeque<>());

                    Deque<Snapshot> deque = history.get(u);
                    // Make room
                    if (deque.size() >= MAX_SNAPSHOTS) deque.removeFirst();
                    // Save current state
                    deque.addLast(new Snapshot(
                            p.getLocation().clone(),
                            p.getHealth(),
                            p.getFoodLevel(),
                            p.getSaturation()
                    ));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // every 20 ticks = 1 second
    }

    /** Restore a player 10 seconds back, if we have the data. */
    public static boolean restore10Sec(Player p) {
        Deque<Snapshot> deque = history.get(p.getUniqueId());
        if (deque == null || deque.size() < MAX_SNAPSHOTS) return false;

        Snapshot snap = deque.peekFirst();  // oldest = ~10 seconds ago

        p.getWorld().spawnParticle(Particle.GUST_EMITTER_LARGE,
                p.getLocation().getX(),
                p.getLocation().getY() + 1.0,
                p.getLocation().getZ(),
                120,    // count
                3.0,   // offsetX
                1.0,   // offsetY
                3.0,
                0.2
        );
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 20f, 1f);

        // Teleport
        if (snap.location.getWorld().equals(p.getWorld())) {
            p.teleport(snap.location);
        } else {
            // if world changed, still teleport
            p.teleport(snap.location);
        }

        // Restore health & food
        p.setHealth(Math.min(snap.health, p.getAttribute(
                org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
        p.setFoodLevel(snap.foodLevel);
        p.setSaturation(snap.saturation);

        return true;
    }

    private static class Snapshot {
        final Location location;
        final double   health;
        final int      foodLevel;
        final float    saturation;

        Snapshot(Location loc, double hp, int food, float sat) {
            this.location   = loc;
            this.health     = hp;
            this.foodLevel  = food;
            this.saturation = sat;
        }
    }
}
