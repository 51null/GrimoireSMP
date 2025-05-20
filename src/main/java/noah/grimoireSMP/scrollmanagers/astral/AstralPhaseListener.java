package noah.grimoireSMP.scrollmanagers.astral;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.AstralScroll;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class AstralPhaseListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent ev) {
        if (!(ev.getEntity() instanceof Player player)) return;

        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null) return;

        // Only active if this is an Astral Scroll and not lost
        if (!(data.getScroll() instanceof AstralScroll) || data.getScroll().isLost()) {
            return;
        }

        int essence = data.getRuneEssence();   // 1–5 (max)
        if (essence >= 5) essence = 5;
        if (essence <= 0) return;

        // Compute phase chance: 3% per essence (so at 5 essence = 15%)
        double chance = 0.04 * essence;
        if (Math.random() < chance) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.3f);
            player.getWorld().spawnParticle(
                    Particle.DUST,
                    player.getLocation().add(0, 1.5, 0),
                    20,     // count
                    0.3, 0.3, 0.3, // offsets
                    0,       // speed
                    new Particle.DustOptions(Color.fromRGB(68, 30, 75), 2.5f)
            );
            ev.setCancelled(true);
        }
    }
}
