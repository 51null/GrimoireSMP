package noah.grimoireSMP.scrollmanagers.temporal;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.TemporalScroll;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class TemporalAwarenessManager {
    // radius in which entities are slowed
    private static final double RADIUS = 5.0;
    // velocity multiplier (50% speed)
    private static final double SLOW_FACTOR = 0.5;

    /** Call this once from your onEnable() to kick off the passive. */
    public static void start(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerScrollData data = PlayerData.getPlayerScrollData(player);
                    // only apply around active Temporal Scrolls
                    if (data == null) continue;
                    if (!(data.getScroll() instanceof TemporalScroll)) continue;
                    if (data.getScroll().getRuneEssence() <= 0) continue;

                    // slow down nearby projectiles / falling hazards
                    player.getWorld().getNearbyEntities(
                            player.getLocation(), RADIUS, RADIUS, RADIUS
                    ).forEach(e -> {
                        if (e instanceof Arrow a) {
                            // Don’t slow arrows fired by a Temporal‐scroll holder
                            ProjectileSource src = a.getShooter();
                            if (src instanceof Player shooter) {
                                PlayerScrollData shootData = PlayerData.getPlayerScrollData(shooter);
                                if (shootData != null
                                        && shootData.getScroll() instanceof TemporalScroll
                                        && !shootData.getScroll().isLost()) {
                                    return;  // skip slowing this arrow
                                }
                            }
                            a.setVelocity(a.getVelocity().multiply(SLOW_FACTOR));
                        }
                        else if (e instanceof TNTPrimed t) {
                            t.setVelocity(t.getVelocity().multiply(SLOW_FACTOR));
                        }
                        else if (e instanceof FallingBlock fb) {
                            fb.setVelocity(fb.getVelocity().multiply(SLOW_FACTOR));
                        }
                    });

                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
