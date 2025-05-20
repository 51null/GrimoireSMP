package noah.grimoireSMP.scrollmanagers.treasure;

import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class WealthHoardListener implements Listener {

    // Define which materials count as ores
    private static final List<Material> ORE_BLOCKS = List.of(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.DIAMOND_ORE,
            Material.LAPIS_ORE,
            Material.EMERALD_ORE,
            Material.COPPER_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE
    );

    /** Double ore drops and XP from mining when active. */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent ev) {
        Player player = ev.getPlayer();
        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null || !data.isWealthHoardingActive()) return;

        Block block = ev.getBlock();
        if (ORE_BLOCKS.contains(block.getType())) {
            // Double the XP
            ev.setExpToDrop(ev.getExpToDrop() * 2);

            // Double the item drops
            ItemStack tool = player.getInventory().getItemInMainHand();
            Collection<ItemStack> drops = block.getDrops(tool);
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    /** Double XP and double mob drops when active. */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent ev) {
        Player killer = ev.getEntity().getKiller();
        if (killer == null) return;

        PlayerScrollData data = PlayerData.getPlayerScrollData(killer);
        if (data == null || !data.isWealthHoardingActive()) return;

        // Double XP orbs
        ev.setDroppedExp(ev.getDroppedExp() * 2);

        // Double item drops
        List<ItemStack> original = List.copyOf(ev.getDrops());
        for (ItemStack drop : original) {
            ev.getDrops().add(drop.clone());
        }
    }
}
