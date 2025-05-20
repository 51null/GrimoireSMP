package noah.grimoireSMP.scrollmanagers.sylvan;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.types.SylvanScroll;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class OneWithNatureListener implements Listener {
    // All “wood log” materials
    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,
            Material.CHERRY_LOG
    );

    /** Fire when a player breaks a block. */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent ev) {
        Player player = ev.getPlayer();
        Block block   = ev.getBlock();
        Material type = block.getType();

        // Must be a wood log
        if (!LOGS.contains(type)) return;

        // Must have a Sylvan Scroll, non-lost, and ≥5 essence
        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null
                || data.getScroll() instanceof SylvanScroll == false
                || data.getScroll().isLost()
                || data.getRuneEssence() <  5) {
            return;
        }

        // Must be “holding” their scroll in either hand (soulbound paper)
        boolean hasScrollInHand = false;
        for (ItemStack hand : new ItemStack[]{
                player.getInventory().getItemInMainHand(),
                player.getInventory().getItemInOffHand()}) {
            if (hand != null
                    && hand.getType() == Material.PAPER
                    && hand.hasItemMeta()
                    && hand.getItemMeta()
                    .getPersistentDataContainer()
                    .has(new org.bukkit.NamespacedKey(
                                    GrimoireSMP.getInstance(),
                                    "scroll"),
                            PersistentDataType.STRING
                    )
            ) {
                hasScrollInHand = true;
                break;
            }
        }
        if (!hasScrollInHand) return;

        // Consume the log (no drop)
        ev.setDropItems(false);
        block.setType(Material.AIR);

        // Give Regeneration IV (amp=3) for 30 ticks (1.5s)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                30,
                3,
                false, // particles
                false  // icon
        ));

        player.sendMessage("§aOne with Nature: You consume the log and feel life surge!");
    }
}
