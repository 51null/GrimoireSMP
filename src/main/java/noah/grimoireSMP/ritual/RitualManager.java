// RitualManager.java
package noah.grimoireSMP.ritual;

import noah.grimoireSMP.*;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RitualManager implements Listener {
    private static RitualManager instance;
    public static RitualManager get() {
        return instance;
    }

    private final JavaPlugin plugin;
    private Location site;                // center of ritual
    private final Map<Location, BlockData> saved = new HashMap<>();
    private RitualSession session;        // only one at a time

    private RitualManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        instance = new RitualManager(plugin);
    }

    /** Build the shrine at `loc`: save blocks & replace around an EnchantingTable */
    public void setupSite(Location loc) {
        this.site = loc.clone().getBlock().getLocation();
        World w = site.getWorld();
        int radius = 3;   // ring of special blocks

        // clear previous if any
        saved.clear();
        if (session != null) session.cancel("Ritual site was reset");

        // Save & set up blocks in a radius around site
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    Location here = site.clone().add(dx, dy, dz);
                    saved.put(here, here.getBlock().getBlockData());
                }
            }
        }

        // central enchanting table
        Block center = site.getBlock();
        center.setType(Material.ENCHANTING_TABLE);

        // ring of special blocks
        Material[] specials = {
                Material.DIAMOND_BLOCK,
                Material.GOLD_BLOCK,
                Material.OBSIDIAN,
                Material.CRYING_OBSIDIAN
        };
        Random r = new Random();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > radius-1 && d <= radius) {
                    Location here = site.clone().add(dx, 0, dz);
                    Material mat = specials[r.nextInt(specials.length)];
                    here.getBlock().setType(mat);
                }
            }
        }
    }

    /** Player right-click table → begin ritual countdown if possible */
    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (session != null) return;                  // already in progress
        if (site == null) return;
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!ev.getClickedBlock().getLocation().equals(site)) return;
        // start session for this player
        session = new RitualSession(ev.getPlayer());
        session.begin();
        ev.setCancelled(true);
    }

    /** Cancel on move too far */
    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        if (session != null && session.player.equals(ev.getPlayer())) {
            ev.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5000, 1, true, false, true));
            if (ev.getTo().distance(session.startLoc) > 75) {
                session.cancel("Moved too far from the Ritual");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent ev) {
        if (session != null && session.player.equals(ev.getEntity())) {
            session.cancel("Died before completing the Ritual");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if (session != null && session.player.equals(ev.getPlayer())) {
            session.cancel("Left the server before completing the Ritual");
        }
    }

    // … optionally onWorldChange, etc. …

    /** Inner class tracking a single active ritual countdown */
    private class RitualSession {
        final Player player;
        final Location startLoc;
        int taskId;
        int remaining = 20 * 60; // seconds

        RitualSession(Player p) {
            this.player = p;
            this.startLoc = p.getLocation().clone();
        }

        void begin() {
            Bukkit.broadcastMessage("§4" + player.getName()
                    + " has begun the Ritual at "
                    + (int) site.getX() + ", ~, " + (int) site.getZ());

            // schedule a sync repeating every second
            taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (remaining <= 0) {
                        complete();
                        return;
                    }
                    // announce each minute
                    if (remaining % 60 == 0) {
                        Bukkit.broadcastMessage("§b" + player.getName()
                                + "'s ritual completes in " + (remaining / 60) + "m");
                    } else if (remaining <= 15) {
                        Bukkit.broadcastMessage("§c" + player.getName()
                                + "'s ritual completes in " + remaining + "s");
                    }
                    remaining--;
                }
            }.runTaskTimer(plugin, 0L, 20L).getTaskId();
        }

        void cancel(String reason) {
           // Broadcast the cancel and free up the session, but do NOT cleanup the site.
                   new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.broadcastMessage("§c" + player.getName()
                                + "'s Ritual was cancelled: " + reason);
                        session = null;
                    }
                }.runTask(plugin);
            Bukkit.getScheduler().cancelTask(taskId);
            }

        void complete() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.broadcastMessage("§a" + player.getName()
                            + " has completed the Ritual!");
                    // teleport on top of table
                    player.teleport(site.clone().add(0.5, 1.2, 0.5));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 99, true, false, true));
                    // Ascension animation
                    startAscension();
                }
            }.runTask(plugin);
            Bukkit.getScheduler().cancelTask(taskId);
        }


        /**
         * Slowly lift player, orbit scrolls, fireworks, then final teleport
         */
        // inside RitualSession:

        void startAscension() {
            // 1) gather *all* registered scroll keys
            List<String> allKeys = ScrollRegistry.getAllScrollNames();
            int total = allKeys.size();
            // 2) spawn an armor stand per scroll
            List<ArmorStand> stands = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                double angle = 2 * Math.PI * i / total;
                Location spawn = site.clone().add(
                        Math.cos(angle) * 2,
                        1.0,
                        Math.sin(angle) * 2
                );
                ArmorStand as = (ArmorStand) player.getWorld()
                        .spawnEntity(spawn, EntityType.ARMOR_STAND);
                as.setVisible(false);
                as.setMarker(true);
                as.setGravity(false);
                // set helmet to the scroll item for key i
                String key = allKeys.get(i);
                Scroll scrollType = ScrollRegistry.getScroll(key).cloneScroll();
                // Wrap in PlayerScrollData so we can call toItemStack
                PlayerScrollData tempData = new PlayerScrollData(scrollType, Scroll.START_RUNE_ESSENCE);
                ItemStack scrollItem = tempData.toItemStack(player.getName());
                as.getEquipment().setHelmet(scrollItem);
                stands.add(as);
            }

            // 3) orbit + lift for ~12 seconds (240 ticks)
            final int duration = 240;
            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    if (tick++ >= duration) {
                        cancel();
                        // remove stands
                        stands.forEach(Entity::remove);

                        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
                        // 4a) if Lost, re-roll
                        if (data.getRuneEssence() == 0) {
                            GrimoireSMP.getInstance().showScrollRoll(player, chosenKey -> {

                                UUID uuid = player.getUniqueId();
                                Scroll template = ScrollRegistry.getScroll(chosenKey);

                                ScrollDataManager sdm = ScrollDataManager.getInstance(plugin);
                                int defaultRune = Scroll.START_RUNE_ESSENCE;
                                boolean grimoire = false;
                                sdm.setPlayerData(uuid, template.getName().toLowerCase(), defaultRune, grimoire);

                                // Load into runtime memory & give item if online


                                    // Reload into memory
                                    PlayerData.loadPlayerData(player);

                                    // Overwrite scroll in memory
                                    PlayerScrollData psd = new PlayerScrollData(template.cloneScroll(),
                                            Scroll.START_RUNE_ESSENCE);
                                    psd.setGrimoire(false);
                                    PlayerData.setPlayerScrollData(uuid, psd);

                                    // Remove any old scroll items
                                    NamespacedKey sk = new NamespacedKey(plugin, "Scroll");
                                    for (ItemStack item : player.getInventory().getContents()) {
                                        if (item != null
                                                && item.getType() == Material.PAPER
                                                && item.hasItemMeta()
                                                && item.getItemMeta()
                                                .getPersistentDataContainer()
                                                .has(sk, PersistentDataType.STRING)) {
                                            player.getInventory().remove(item);
                                        }
                                    }

                                    // Give fresh one
                                    PlayerData.updateScrollItem(player);
                            });
                        } else {
                            // 4b) give a Switch Case item
                            ItemStack switchCase = new ItemStack(Material.PAPER);
                            ItemMeta m = switchCase.getItemMeta();
                            m.setDisplayName("§dSwitch Case");

                            // *** Add this line to give it a unique C.M.D. for your resource pack:
                            m.setCustomModelData(Constants.SWITCH_CASE_MODEL_DATA);

                            // Tag it so only your code recognizes it
                            NamespacedKey scKey = new NamespacedKey(GrimoireSMP.getInstance(), "switch_case");
                            m.getPersistentDataContainer().set(scKey, PersistentDataType.BYTE, (byte)1);

                            switchCase.setItemMeta(m);
                            player.getInventory().addItem(switchCase);
                        }

                        // final teleport after 3s
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Random r = new Random();
                                int dx = r.nextInt(1001) - 500;
                                int dz = r.nextInt(1001) - 500;
                                Location dest = site.clone().add(dx, 0, dz);
                                dest.setY(dest.getWorld().getHighestBlockYAt(dest));
                                player.teleport(dest.add(0.5, 1, 0.5));
                                cleanupSite();
                                session = null;
                            }
                        }.runTaskLater(plugin, 60L);

                        return;
                    }

                    // 3a) lift the player smoothly to 3 blocks over duration
                    double liftY = 1.0 + (2.0 * tick / duration);
                    player.teleport(site.clone().add(0.5, liftY, 0.5));
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0f, 0.005f * tick);
                    // 3b) orbit each stand, and firework spark on close approach
                    for (int i = 0; i < stands.size(); i++) {
                        ArmorStand as = stands.get(i);
                        double baseAngle = 2 * Math.PI * i / stands.size();
                        double currentAngle = baseAngle + tick * 0.02;
                        Location pos = site.clone().add(
                                Math.cos(currentAngle) * 2,
                                liftY,
                                Math.sin(currentAngle) * 2
                        );
                        as.teleport(pos);
                        if (pos.distance(player.getLocation()) < 0.5) {
                            player.getWorld().spawnParticle(
                                    Particle.FIREWORK,
                                    pos, 8, 0.1, 0.1, 0.1, 0.05
                            );
                        }
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);
        }


        /**
         * Restore saved blocks at site
         */
        private void cleanupSite() {
            saved.forEach((loc, data) -> loc.getBlock().setBlockData(data, false));
        }
    }
}
