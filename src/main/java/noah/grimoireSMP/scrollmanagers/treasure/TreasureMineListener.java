package noah.grimoireSMP.scrollmanagers.treasure;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.TreasureScroll;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TreasureMineListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent ev) {
        // 1) Must be player → player
        if (!(ev.getDamager() instanceof Player damager)) return;
        if (!(ev.getEntity()   instanceof Player victim))  return;

        PlayerScrollData data = PlayerData.getPlayerScrollData(damager);
        if (data == null) return;

        // 2) Must be treasure scroll, not lost
        if (!(data.getScroll() instanceof TreasureScroll)
                || data.getScroll().getRuneEssence() <= 0) {
            return;
        }

        int essence = data.getRuneEssence();
        // 3) Compute extra durability hits
        int extra = Math.round(essence / 3.0f);
        if (extra <= 0) return;

        // 4) Damage victim's armor
        var eqV = victim.getEquipment();
        if (eqV != null) {
            applyDurabilityDamage(eqV.getHelmet(),   extra, victim, eqV::setHelmet);
            applyDurabilityDamage(eqV.getChestplate(),extra, victim, eqV::setChestplate);
            applyDurabilityDamage(eqV.getLeggings(), extra, victim, eqV::setLeggings);
            applyDurabilityDamage(eqV.getBoots(),     extra, victim, eqV::setBoots);

            var eqD = damager.getEquipment();
            if (eqD != null) {
                applyDurabilityHeal(eqD.getHelmet(),   extra, damager, eqD::setHelmet);
                applyDurabilityHeal(eqD.getChestplate(),extra, damager, eqD::setChestplate);
                applyDurabilityHeal(eqD.getLeggings(), extra, damager, eqD::setLeggings);
                applyDurabilityHeal(eqD.getBoots(),     extra, damager, eqD::setBoots);
            }
        }

        // 5) If grimoire, heal damager's armor instead
    }

    /**
     * Increases the Damageable meta by `amount`, breaking the item
     * if damage exceeds max. `setter` puts the modified stack back.
     */
    private void applyDurabilityDamage(ItemStack item,
                                       int amount,
                                       Player victim,
                                       java.util.function.Consumer<ItemStack> setter) {
        if (item == null) return;
        var meta = item.getItemMeta();
        if (!(meta instanceof Damageable dmg)) return;

        int current = dmg.getDamage();
        int maxD    = item.getType().getMaxDurability();
        int updated = current + amount;
        if (updated >= maxD) {
            // break it
            setter.accept(null);
        } else {
            dmg.setDamage(updated);
            item.setItemMeta(dmg);
            setter.accept(item);
        }
    }

    /**
     * Decreases the Damageable meta by `amount`, clamped at zero.
     */
    private void applyDurabilityHeal(ItemStack item,
                                     int amount,
                                     Player player,
                                     java.util.function.Consumer<ItemStack> setter) {
        if (item == null) return;
        var meta = item.getItemMeta();
        if (!(meta instanceof Damageable dmg)) return;

        int current = dmg.getDamage();
        int updated = current - amount;
        if (updated < 0) updated = 0;
        dmg.setDamage(updated);
        item.setItemMeta(dmg);
        setter.accept(item);
    }
}
