package noah.grimoireSMP.listener;

import noah.grimoireSMP.Constants;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RuneConsumeListener implements Listener {

    @EventHandler
    public void onPlayerUseRune(PlayerInteractEvent event) {
        // Only care about right‐click
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // Figure out which hand was used
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        Player player = event.getPlayer();
        // This gives you the item in *that* hand
        ItemStack item = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (item == null) return;
        if (item.getType() != Material.COAL) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()
                || meta.getCustomModelData() != Constants.RUNE_MODEL_DATA) return;

        event.setCancelled(true);

        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null) return;

        // Cannot use if Lost
        if (data.getRuneEssence() == 0) {
            player.sendMessage("§b§oYour scroll is §r§f§lLOST§r§b§o. "
                    + "You cannot use runes until you perform the Ritual.");
            return;
        }

        // If not at max, consume
        if (data.getRuneEssence() < Scroll.MAX_RUNE_ESSENCE) {
            data.addRuneEssence(1);
            player.sendMessage("§bYou consumed a Rune and gained 1 Rune Essence! "
                    + "Current Essence: " + data.getRuneEssence());
            PlayerData.updateScrollItem(player);

            // *** Remove one from the correct hand ***
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                if (hand == EquipmentSlot.OFF_HAND) {
                    player.getInventory().setItemInOffHand(item);
                } // main hand auto‐updates
            } else {
                // Single rune — clear that slot
                if (hand == EquipmentSlot.HAND) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }
        } else {
            player.sendMessage("§bYour Rune Essence is already at maximum!");
        }
    }
}

