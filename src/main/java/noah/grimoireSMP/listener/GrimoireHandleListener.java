package noah.grimoireSMP.listener;

import noah.grimoireSMP.Constants;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GrimoireHandleListener implements Listener {

    @EventHandler
    public void onPlayerUseGrimoireHandle(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();

        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        // Grab the item from the correct hand
        ItemStack item = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        // Check that the item is a Grimoire Handle.
        if (item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() &&
                item.getItemMeta().getCustomModelData() == Constants.GRIMOIRE_HANDLE_MODEL_DATA) {

            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data == null) return;
            if (data.getRuneEssence() == 0) {
                player.sendMessage("§b§oYour scroll is §r§f§lLOST§r§b§o. You cannot upgrade it.");
                return;
            }
            if (data.isGrimoire()) {
                player.sendMessage("§b§oYour scroll is already a Grimoire.");
                return;
            }

            // Perform the upgrade
            data.setGrimoire(true);
            PlayerData.updateScrollItem(player);
            player.sendMessage("§b§oYour scroll has been upgraded to a Grimoire!");


            // Remove one handle from the correct hand
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                if (hand == EquipmentSlot.OFF_HAND) {
                    player.getInventory().setItemInOffHand(item);
                }
                // main‐hand slot updates automatically
            } else {
                // clear that hand slot
                if (hand == EquipmentSlot.HAND) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }

            event.setCancelled(true);
        }
        }
    }

