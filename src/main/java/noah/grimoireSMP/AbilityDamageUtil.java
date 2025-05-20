package noah.grimoireSMP;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
/**
 * Utility for dealing armor-bypassing ability damage.
 */
public final class AbilityDamageUtil {
    private AbilityDamageUtil() {}  // no instantiation

    public static void dealAbilityDamage(Player damager, LivingEntity target, double hpToDeal) {
        if (target.isDead() || target.getHealth() <= 0) {
            return;
        }

        double finalHealth = target.getHealth() - hpToDeal;
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                damager, target, DamageCause.CUSTOM, hpToDeal
        );

        Bukkit.getPluginManager().callEvent(damageEvent);

        if (!damageEvent.isCancelled()) {
            if (finalHealth <= 0) {
                target.setLastDamageCause(damageEvent); // Makes sure death links to this event
                target.setHealth(0); // Still forcibly kills, but more properly linked
            } else {
                target.setHealth(finalHealth);
            }
        }
    }

}
