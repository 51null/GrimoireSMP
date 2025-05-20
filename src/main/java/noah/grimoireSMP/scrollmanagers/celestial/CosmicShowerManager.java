package noah.grimoireSMP.scrollmanagers.celestial;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static noah.grimoireSMP.AbilityDamageUtil.dealAbilityDamage;

public class CosmicShowerManager implements Listener {
    private static final List<Material> METEOR_MATS = List.of(
            Material.SANDSTONE,
            Material.YELLOW_GLAZED_TERRACOTTA,
            Material.GOLD_BLOCK,
            Material.QUARTZ_BLOCK,
            Material.CHISELED_QUARTZ_BLOCK,
            Material.END_STONE,
            Material.DIORITE,
            Material.RAW_GOLD_BLOCK,
            Material.RAW_IRON_BLOCK
    );
    private static final NamespacedKey KEY_CASTER =
            new NamespacedKey(GrimoireSMP.getInstance(), "cosmic_shower_caster");
    private static final Random RNG = new Random();

    /**
     * @param caster         the player who called it
     * @param targetCenter   center of the impact area
     * @param totalMeteors   how many meteors to spawn
     * @param areaRadius     horizontal radius around targetCenter
     * @param delayTicks     ticks between each meteor spawn
     * @param clusterSize    edge length of the cubic meteor (e.g. 2 → 2×2×2)
     */
    public static void spawnShower(Player caster,
                                   Location targetCenter,
                                   int totalMeteors,
                                   double areaRadius,
                                   long delayTicks,
                                   int clusterSize) {

        World world = targetCenter.getWorld();
        new BukkitRunnable() {
            int spawned = 0;
            @Override
            public void run() {
                if (spawned++ >= totalMeteors) {
                    cancel();
                    return;
                }
                for (int deg = 0; deg < 360; deg += 3) {
                    double rad = Math.toRadians(deg);
                    double x = Math.cos(rad) * areaRadius;
                    double z = Math.sin(rad) * areaRadius;
                    Location spawn = targetCenter.clone().add(x, 0, z);
                    // a small burst of starlight
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 0.1, 0), 2, 0, 0, 0, 0.01);
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 1, 0), 2, 0, 0, 0, 0.01);
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 2, 0), 2, 0, 0, 0, 0.01);
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 3, 0), 2, 0, 0, 0, 0.01);
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 4, 0), 2, 0, 0, 0, 0.01);
                    caster.getWorld().spawnParticle(Particle.END_ROD, spawn.add(0, 5, 0), 2, 0, 0, 0, 0.01);
                }

                // pick a random horizontal offset
                double angle = RNG.nextDouble() * Math.PI * 2;
                double dist  = RNG.nextDouble() * areaRadius;
                double dx = Math.cos(angle) * dist;
                double dz = Math.sin(angle) * dist;

                // spawn height
                double height = 30 + RNG.nextDouble() * 10;
                Location base = targetCenter.clone().add(dx, height, dz);

                // choose a random material for this meteor
                Material mat = METEOR_MATS.get(RNG.nextInt(METEOR_MATS.size()));
                BlockData data = mat.createBlockData();

                // spawn a cluster of falling blocks of size clusterSize
                int half = clusterSize / 2;
                for (int x = -half; x < -half + clusterSize; x++) {
                    for (int y = -half; y < -half + clusterSize; y++) {
                        for (int z = -half; z < -half + clusterSize; z++) {
                            Location spawnLoc = base.clone().add(x, y, z);
                            FallingBlock fb = world.spawnFallingBlock(spawnLoc, data);
                            fb.setDropItem(false);
                            fb.setHurtEntities(false);
                            fb.getPersistentDataContainer().set(
                                    KEY_CASTER, PersistentDataType.STRING,
                                    caster.getUniqueId().toString()
                            );
                            // velocity: mostly downward, slight random drift
                            fb.setVelocity(new Vector(
                                    (RNG.nextDouble() - 0.5) * 0.2,
                                    -1.5,
                                    (RNG.nextDouble() - 0.5) * 0.2
                            ));
                        }
                    }
                }
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0L, delayTicks);
    }

    @EventHandler
    public void onMeteorLand(EntityChangeBlockEvent ev) {
        if (!(ev.getEntity() instanceof FallingBlock fb)) return;
        var pdc = fb.getPersistentDataContainer();
        if (!pdc.has(KEY_CASTER, PersistentDataType.STRING)) return;

        ev.setCancelled(true);
        Location loc = ev.getBlock().getLocation();
        World world = loc.getWorld();
        fb.remove();

        String shooterId = pdc.get(KEY_CASTER, PersistentDataType.STRING);
        UUID shooterUUID = UUID.fromString(shooterId);
        Player shootshoot = Bukkit.getPlayer(shooterId);
        // explosion + damage
        world.createExplosion(loc, 2.5f, false, false, Bukkit.getPlayer(shooterUUID));

        Particle.DustTransition dustTransition = new Particle.DustTransition(Color.fromRGB(241, 227, 164), Color.fromRGB(250, 249, 208), 2.0F);
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 90, 1.6, 2, 1.6, 0.01, dustTransition);
        world.spawnParticle(Particle.WAX_OFF, loc, 118, 2.25, 0.3, 2.25, 0.05);
        world.spawnParticle(Particle.WAX_ON, loc, 84, 2.25, 0.3, 2.25, 0.05);
        world.spawnParticle(Particle.END_ROD, loc, 84, 2.25, 0.3, 2.25, 0.05);

        world.getNearbyEntities(loc, 6, 6, 6).forEach(ent -> {
            if (ent instanceof Player target && !target.getUniqueId().equals(shooterUUID)) {
                dealAbilityDamage(shootshoot, target, 2);
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        15 * 20, 1, false, false, true
                ));
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        15 * 20, 1, false, false, true
                ));
            }
        });
    }
}

