package noah.grimoireSMP.scrollmanagers.temporal;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TemporalHiatusManager implements Listener {
    private static final double RADIUS = 5.0;


    private static final Set<UUID>    frozenPlayers  = new HashSet<>();
    private static final Set<Integer> frozenEntities = new HashSet<>();
    private static JavaPlugin plugin;

    /** Call once in onEnable() */
    public static void init(JavaPlugin pl) {
        plugin = pl;
        Bukkit.getPluginManager().registerEvents(new TemporalHiatusManager(), pl);
    }

    /** Triggers a 2.5s freeze around caster. */
    public static void triggerHiatus(Player caster, long DURATION) {
        World w = caster.getWorld();
        Set<UUID>    toUnfreezePlayers  = new HashSet<>();
        Set<LivingEntity> toUnfreezeEntities = new HashSet<>();
        w.playSound(caster.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 2.0f, 1.1f);
        for (Entity e : w.getNearbyEntities(caster.getLocation(), RADIUS, RADIUS, RADIUS)) {
            if (e.equals(caster)) continue;

            if (e instanceof Player target) {
                frozenPlayers.add(target.getUniqueId());
                toUnfreezePlayers.add(target.getUniqueId());
                target.sendTitle("§b§lTIME STOPPED", "", 0, (int) DURATION, 0);
            } else if (e instanceof LivingEntity mob) {
                frozenEntities.add(mob.getEntityId());
                toUnfreezeEntities.add(mob);
                mob.setAI(false);
                mob.setVelocity(new Vector(0,0,0));
            }
        }

        // Schedule unfreeze after DURATION
        new BukkitRunnable() {
            @Override
            public void run() {
                // Unfreeze players
                for (UUID u : toUnfreezePlayers) frozenPlayers.remove(u);

                // Unfreeze mobs
                for (LivingEntity mob : toUnfreezeEntities) {
                    frozenEntities.remove(mob.getEntityId());
                    if (mob.isValid()) mob.setAI(true);
                }
            }
        }.runTaskLater(plugin, DURATION);
    }

    /** Prevent any frozen player from moving. */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        if (frozenPlayers.contains(ev.getPlayer().getUniqueId())) {
            ev.setCancelled(true);
            // snap them back so no “rubber-band”
            ev.getPlayer().teleport(ev.getFrom());
        }
    }
}
