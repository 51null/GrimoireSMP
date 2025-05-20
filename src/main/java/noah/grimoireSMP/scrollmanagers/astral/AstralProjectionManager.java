package noah.grimoireSMP.scrollmanagers.astral;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AstralProjectionManager implements Listener {

    private static final Map<UUID, ProjectionSession> sessions = new HashMap<>();

    /** Call this to start projection mode for a player. */
    public static void startProjection(Player player) {
        UUID uuid = player.getUniqueId();
        if (sessions.containsKey(uuid)) return;  // already projecting

        // 1) Save original location
        Location original = player.getLocation().clone();

        // 2) Switch to SPECTATOR


        // 3) Spawn a pose‐matched armor stand at original
        ArmorStand stand = original.getWorld().spawn(original, ArmorStand.class, as -> {
            as.setBasePlate(false);
            as.setGravity(false);
            as.setArms(true);
            as.setVisible(true);
            as.setMarker(false);
            as.setInvulnerable(false);
            // copy armor & head
            as.getEquipment().setHelmet(player.getInventory().getHelmet());
            as.getEquipment().setChestplate(player.getInventory().getChestplate());
            as.getEquipment().setLeggings(player.getInventory().getLeggings());
            as.getEquipment().setBoots(player.getInventory().getBoots());
            // pose in radians
            as.setBodyPose(new EulerAngle(Math.toRadians(12), 0, 0));
            as.setHeadPose(new EulerAngle(Math.toRadians(28), 0, 0));
            as.setLeftLegPose(new EulerAngle(Math.toRadians(316), Math.toRadians(17), Math.toRadians(60)));
            as.setRightLegPose(new EulerAngle(Math.toRadians(316), Math.toRadians(360), Math.toRadians(275)));
            as.setLeftArmPose(new EulerAngle(Math.toRadians(332), Math.toRadians(19), Math.toRadians(32)));
            as.setRightArmPose(new EulerAngle(Math.toRadians(330), 0, Math.toRadians(316)));

        });
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(178, 75, 243), 2f);

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(player.getLocation().add(0, 10, 0));
        player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation(), 260, 0.5f, 0.5, 0.5f, dust);

        // 4) Schedule radius‐check every second
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getGameMode() != GameMode.SPECTATOR) {
                    endProjection(uuid);
                    return;
                }
                // if out of 150-block radius
                if (player.getLocation().distanceSquared(original) > 150*150) {
                    endProjection(uuid);
                    player.sendMessage("§bYour§r §5Astral Projection§r §bwent out of bounds!");
                }
                player.getWorld().spawnParticle(Particle.DUST, original, 80, 0.5f, 0, 0.5f, dust);
                player.getWorld().spawnParticle(Particle.DUST, original.clone().add(0, 0.3f, 0), 80, 0.2f, 0.2, 0.2f, 0.02, dust);
                player.getWorld().spawnParticle(Particle.DUST, original.clone().add(0, 1.62f, 0), 30, 0.1f, 0.1, 0.1f, 0.01, dust);
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, 10L);

        // 5) Store session
        sessions.put(uuid, new ProjectionSession(original, stand, task));
    }

    /** Ends projection for that player, if any. */
    public static void endProjection(UUID uuid) {
        ProjectionSession s = sessions.remove(uuid);
        if (s == null) return;

        // cancel radius task
        s.task.cancel();

        // restore player
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(s.originalLocation);
        }
        // remove stand
        if (s.stand != null && !s.stand.isDead()) {
            s.stand.remove();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        UUID uuid = ev.getPlayer().getUniqueId();
        ProjectionSession s = sessions.get(uuid);
        if (s == null) return;

        // if they step onto the stand’s block
        Location ploc = ev.getTo();
        if (ploc.getWorld().equals(s.stand.getWorld())
                && ploc.distanceSquared(s.stand.getLocation()) < 0.5*0.5) {
            endProjection(uuid);
        }
    }

    @EventHandler
    public void onStandDamaged(EntityDamageByEntityEvent ev) {
        Entity victim = ev.getEntity();

        for (var entry : sessions.entrySet()) {
            UUID ownerUuid        = entry.getKey();
            ProjectionSession s   = entry.getValue();

            // If *this* stand was hit
            if (s.stand.getUniqueId().equals(victim.getUniqueId())) {
                ev.setCancelled(true);      // stop the stand from actually taking damage

                // End the projection for *that* stand’s owner
                endProjection(ownerUuid);
                return;
            }
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent ev) {
        ArmorStand clicked = ev.getRightClicked();
        // If this stand belongs to *any* active projection, cancel
        for (ProjectionSession s : sessions.values()) {
            if (clicked.getUniqueId().equals(s.stand.getUniqueId())) {
                ev.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
        endProjection(ev.getPlayer().getUniqueId());
    }

    /** Holds all the data for one player’s projection. */
    private static class ProjectionSession {
        final Location    originalLocation;
        final ArmorStand  stand;
        final BukkitTask  task;

        ProjectionSession(Location original, ArmorStand stand, BukkitTask task) {
            this.originalLocation = original;
            this.stand            = stand;
            this.task             = task;
        }
    }
}
