package noah.grimoireSMP.listener;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static noah.grimoireSMP.scroll.types.SplashScroll.fallImmunity;

public class SelfDamageCancelListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball) {
            Fireball fb = (Fireball) event.getDamager();
            if (fb.getShooter() instanceof Player) {
                Player shooter = (Player) fb.getShooter();
                // If the entity being damaged is the shooter, cancel the damage.
                if (event.getEntity().equals(shooter)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Long expiry = fallImmunity.get(player.getUniqueId());

            if (expiry != null && System.currentTimeMillis() <= expiry) {
                event.setCancelled(true); // Cancel the fall damage
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        fallImmunity.remove(event.getPlayer().getUniqueId());
    }

}
