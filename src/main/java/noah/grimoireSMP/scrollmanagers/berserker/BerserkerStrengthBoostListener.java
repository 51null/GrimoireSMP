package noah.grimoireSMP.scrollmanagers.berserker;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.BerserkerScroll;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

/**
 * Listens for Strength potions being applied to players
 * and boosts the amplifier by +1 if they have a Berserker Scroll.
 */
public class BerserkerStrengthBoostListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
        if (!(ev.getDamager() instanceof Player damager)) return;

        PlayerScrollData data = PlayerData.getPlayerScrollData(damager);
        if (data == null
                || !(data.getScroll() instanceof BerserkerScroll)
                || data.getScroll().isLost()
        ) return;

        // Check if the player has a Strength effect
        PotionEffect str = damager.getPotionEffect(PotionEffectType.STRENGTH);
        if (str == null) return;

        // amplifier: 0 = Strength I, 1 = Strength II, etc.
        int amp = str.getAmplifier();

        // Each amplifier step gives +3 damage, so we add one more step:
        double extra = 3.0 * 1;  // +3 for the “boosted” level

        ev.setDamage(ev.getDamage() + extra);
    }
}
