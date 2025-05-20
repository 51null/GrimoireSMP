// BloodMarkManager.java
package noah.grimoireSMP.scrollmanagers.berserker;

import net.md_5.bungee.api.ChatMessageType;
import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;

public class BloodMarkManager implements Listener {
    private static final NamespacedKey BLOOD_KEY =
            new NamespacedKey(GrimoireSMP.getInstance(), "blood_marked");

    // Map<Tracker UUID, Target UUID>
    private final Map<UUID, UUID> tracking = new HashMap<>();
    private static BloodMarkManager instance;
    public final Map<UUID, Long> trackingCooldowns = new HashMap<>();


    public BloodMarkManager() {

        // schedule the particle & action-bar task every tick
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 900) {
                    cancel();
                    return;
                }
                for (Map.Entry<UUID, UUID> e: tracking.entrySet()) {
                    Player tracker = Bukkit.getPlayer(e.getKey());
                    Player target  = Bukkit.getPlayer(e.getValue());
                    if (tracker == null || target == null) {
                        tracking.remove(e.getKey());
                        continue;
                    }
                    // 1) Directional particle
                    Location tLoc = tracker.getEyeLocation().add(0f, 1.5f, 0);
                    Location tgtLoc = target.getEyeLocation();
                    Vector dir = tgtLoc.toVector().subtract(tLoc.toVector()).normalize();

                    int points = 20;
                    // Distance between each point
                    double spacing = 0.3;

                    for (int i = 1; i <= points; i++) {
                        Location point = tLoc.clone().add(dir.clone().multiply(i * spacing));
                        tracker.spawnParticle(
                                Particle.DUST,
                                point,
                                1,
                                new Particle.DustOptions(Color.RED, 3f)
                        );
                    }
                    // 2) Notify target
                    target.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new net.md_5.bungee.api.chat.TextComponent("5§lYou Feel Watched...§r")
                    );
                    target.sendTitle(
                            "§5§lYou Feel Watched...§r",
                            "",
                            5, 40, 5
                    );
                }
                tick++;
            }
        }.runTaskTimer(GrimoireSMP.getInstance(), 0, 20);
    }

    /** 1) On player kill, maybe drop one random armor piece blood‑marked. */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev) {
        Player victim = ev.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        PlayerScrollData kd = PlayerData.getPlayerScrollData(killer);
        if (kd == null || !kd.isGrimoire()) return;

        if (Math.random() > 0.6) return;  // 30% chance

        // Collect non‑air armor pieces
        ItemStack[] armor = victim.getInventory().getArmorContents();
        List<Integer> slots = List.of(EquipmentSlot.HEAD.ordinal(),
                EquipmentSlot.CHEST.ordinal(),
                EquipmentSlot.LEGS.ordinal(),
                EquipmentSlot.FEET.ordinal());
        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() != Material.AIR) {
                validSlots.add(i);
            }
        }
        if (validSlots.isEmpty()) return;

        // Pick one random slot to blood‑mark
        int chosenIdx = validSlots.get(new Random().nextInt(validSlots.size()));
        ItemStack original = armor[chosenIdx];
        ItemStack bloodMarked = original.clone();

        // Tag it
        ItemMeta meta = bloodMarked.getItemMeta();
        meta.getPersistentDataContainer().set(
                BLOOD_KEY, PersistentDataType.STRING,
                victim.getUniqueId().toString()
        );
        List<String> lore = meta.getLore() == null
                ? new ArrayList<>() : meta.getLore();
        lore.add("§cBlood Marked: " + victim.getName());
        meta.setLore(lore);
        bloodMarked.setItemMeta(meta);

        // *** Remove the original from inventory so Vanilla won't drop it ***
        switch (chosenIdx) {
            case 0 -> victim.getInventory().setHelmet(null);
            case 1 -> victim.getInventory().setChestplate(null);
            case 2 -> victim.getInventory().setLeggings(null);
            case 3 -> victim.getInventory().setBoots(null);
        }

        // *** Remove any raw copy of that material from the default drop list ***
        Material mat = original.getType();
        ev.getDrops().removeIf(item -> item.getType() == mat);

        // Finally, drop only the blood‑marked piece
        victim.getWorld().dropItemNaturally(victim.getLocation(), bloodMarked);
    }

    /** 2) When the GUI closes, bind the marked piece to start tracking **/
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        if (ev.getView().getType() != InventoryType.HOPPER) return;
        if (!"Berserker's Instinct".equals(ev.getView().getTitle())) return;

        HumanEntity he = ev.getPlayer();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        Inventory inv = ev.getInventory();
        // look for a single blood‑marked armor piece
        for (ItemStack it: inv.getContents()) {
            if (it == null || !it.hasItemMeta()) continue;
            String victimUuid = it.getItemMeta()
                    .getPersistentDataContainer()
                    .get(BLOOD_KEY, PersistentDataType.STRING);
            if (victimUuid != null) {
                // start tracking
                UUID targetId = UUID.fromString(victimUuid);
                tracking.put(p.getUniqueId(), targetId);
                trackingCooldowns.put(p.getUniqueId(),
                        System.currentTimeMillis() + (12 * 3600000L)
                );
                p.sendMessage("§c§lYou now track §r§m" + Bukkit.getOfflinePlayer(targetId).getName());
                inv.remove(it);
                break;
            }
        }
    }

    /** 3) Prevent wearing Blood‑Marked armor — revert on equip **/
    @EventHandler
    public void onArmorEquip(InventoryClickEvent ev) {
        if (ev.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (!(ev.getWhoClicked() instanceof Player)) return;

        ItemStack cursor = ev.getCursor(); // item being placed
        if (cursor == null || !cursor.hasItemMeta()) return;
        if (cursor.getItemMeta().getPersistentDataContainer()
                .has(BLOOD_KEY, PersistentDataType.STRING))
        {
            // revert: simply remove the tag and lore
            ItemMeta meta = cursor.getItemMeta();
            meta.getPersistentDataContainer().remove(BLOOD_KEY);
            List<String> lore = meta.getLore();
            if (lore != null) {
                lore.removeIf(line -> line.startsWith("§cBlood Marked"));
                meta.setLore(lore);
            }
            cursor.setItemMeta(meta);
            ((Player)ev.getWhoClicked())
                    .sendMessage("§eYou wear the marked armor—it returns to normal.");
        }
    }
}
