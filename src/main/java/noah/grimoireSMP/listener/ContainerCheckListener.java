package noah.grimoireSMP.listener;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;

public class ContainerCheckListener implements Listener {

    // Use the same NamespacedKey as used in PlayerScrollData.toItemStack
    private final NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "scroll");

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        // Only check if the top inventory is a container and not the player's own inventory.
        if (topInv.getType() == InventoryType.PLAYER) {
            return;
        }
        // Loop through the container inventory.
        for (int i = 0; i < topInv.getSize(); i++) {
            ItemStack item = topInv.getItem(i);
            if (item == null) continue;
            if (item.getType() == org.bukkit.Material.PAPER && item.hasItemMeta()) {
                // Check if this item is tagged as a scroll.
                if (item.getItemMeta().getPersistentDataContainer().has(scrollKey, PersistentDataType.STRING)) {
                    // Remove the item (set slot to null)
                    topInv.setItem(i, null);
                    // Optionally, notify the player.
                }
            }
        }
    }
}
