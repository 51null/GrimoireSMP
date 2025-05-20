package noah.grimoireSMP.scrollmanagers.temporal;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.TemporalScroll;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import static noah.grimoireSMP.scroll.types.TemporalScroll.isAuraActive;

public class ClaritySurgeListener implements Listener {
    // How far (min–max) we teleport on a successful surge
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 6.0;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent ev) {
        if (!(ev.getEntity() instanceof Player player)) return;

        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null) return;

        // Must be a Temporal Scroll, not lost
        if (!(data.getScroll() instanceof TemporalScroll) || data.getScroll().isLost())
            return;
        if (!isAuraActive(player)) return;
        int essence    = data.getRuneEssence();
        boolean grimoire = data.isGrimoire();

        // Only active when essence > 5 or grimoire
        if (essence < 5) return;


        double chance = (grimoire ? 0.22 : 0.10);

        if (Math.random() < chance) {
            Location from = player.getLocation();
            World world   = from.getWorld();

            // random horizontal offset
            double angle  = Math.random() * Math.PI * 2;
            double radius = MIN_RADIUS + Math.random() * (MAX_RADIUS - MIN_RADIUS);
            double dx      = Math.cos(angle) * radius;
            double dz      = Math.sin(angle) * radius;

            // candidate XZ
            double x = from.getX() + dx;
            double z = from.getZ() + dz;
            int baseY = from.getBlockY();

            // try to find a safe spot at or below baseY first
            Location dest = null;
            for (int dy = 0; dy <= 5; dy++) {
                // check one block down each step, then later up
                for (int sign : new int[]{-1, 1}) {
                    int y = baseY + sign * dy;
                    // must be in valid world range
                    if (y < world.getMinHeight() || y > world.getMaxHeight()) continue;

                    Location check = new Location(world, x, y, z);
                    // two-block clearance: feet and head
                    if (check.getBlock().getType() == Material.AIR
                            && check.clone().add(0,1,0).getBlock().getType() == Material.AIR
                            // and block below is solid so you don't fall
                            && check.clone().add(0,-1,0).getBlock().getType().isSolid()) {
                        dest = check.add(0, 0, 0); // clone
                        break;
                    }
                }
                if (dest != null) break;
            }

            // fallback to highest if all else fails
            if (dest == null) {
                int y = world.getHighestBlockYAt((int)x, (int)z) + 1;
                dest = new Location(world, x, y, z);
            }

// perform the teleport
            player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage("§e§lClarity Surge!§r§b§o You phase away to safety.");
            world.spawnParticle(Particle.CLOUD, from, 30, 0.5,0.5,0.5, 0.02);
            world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);

        }
    }
}
