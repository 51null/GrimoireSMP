package noah.grimoireSMP.listener;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ScrollProtectionListener implements Listener {

    // Create a NamespacedKey for identifying scroll items.
    // (You might want to centralize this in a Constants class.)
    private final NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");

    /**
     * Checks if an item is the player's soulbound scroll by checking
     * for our persistent data tag.
     */
    private boolean isSoulboundScroll(ItemStack item, Player player) {
        if (item == null || item.getType() != Material.PAPER) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(scrollKey, PersistentDataType.STRING)) return false;

        // The stored scroll type (e.g., "Infernal Scroll")
        String storedType = pdc.get(scrollKey, PersistentDataType.STRING);
        if (storedType == null) return false;

        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null) return false;
        // Compare ignoring case.
        return storedType.equalsIgnoreCase(data.getScroll().getName());
    }

    // Prevent dropping the scroll.
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        /*ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        player.sendMessage("1");

        boolean holdingScroll = false;

        // Create your key (it should be the same used in your scroll tagging)
        NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");

        if (isScroll(mainHand, scrollKey)) {
            holdingScroll = true;
        } else if (isScroll(offHand, scrollKey)) {
            holdingScroll = true;
        }
        player.sendMessage("2");
        if (holdingScroll) {
            player.sendMessage("3");
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data != null) {
                player.sendMessage("4");
                data.getScroll().useActiveAbility(2, player);
            }
        }
        player.sendMessage("5");*/
        if (isSoulboundScroll(event.getItemDrop().getItemStack(), player)) {
            event.setCancelled(true);
        }
    }
    private boolean isScroll(ItemStack item, NamespacedKey scrollKey) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        GrimoireSMP.getInstance().getLogger().info("got it");
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(scrollKey, PersistentDataType.STRING);
    }

    /**
     * Allow internal inventory movements, but cancel clicks that try
     * to move the scroll out of the player's personal inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (!isSoulboundScroll(clickedItem, player)) return;

        // Check which inventory is being clicked.
        // We allow changes if the clicked inventory is the player's own inventory.
        if (event.getClickedInventory() == null ||
                !event.getClickedInventory().getType().toString().equalsIgnoreCase("CHEST")) {
            // If the click is within the player's inventory (the bottom inventory) allow it.
            return;
        }
        // Otherwise, cancel the event.
        event.setCancelled(true);
        player.sendMessage("You cannot move your soulbound scroll into a container.");
    }

    // Prevent dragging the scroll into a container.
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        for (ItemStack item : event.getNewItems().values()) {
            if (isSoulboundScroll(item, player)) {
                // If the destination is not the player's inventory, cancel.
                if (!event.getView().getBottomInventory().equals(event.getInventory())) {
                    event.setCancelled(true);
                    player.sendMessage("\uE001");
                    player.sendMessage("You cannot drag your soulbound scroll into a container.");
                    break;
                }
            }
        }
    }

    // When the inventory is closed, check if the scroll is missing in the player's inventory.
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!hasScrollItem(player)) {
            PlayerData.updateScrollItem(player);
        }
    }

    // Utility: Check if the player's inventory contains their soulbound scroll.
    private boolean hasScrollItem(Player player) {
        PlayerScrollData data = PlayerData.getPlayerScrollData(player);
        if (data == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isSoulboundScroll(item, player)) {
                return true;
            }
        }
        return false;
    }


    private static boolean hasScrollItemStatic(Player player) {
        PlayerScrollData data = noah.grimoireSMP.PlayerData.getPlayerScrollData(player);
        if (data == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.PAPER &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getPersistentDataContainer().has(
                            new NamespacedKey(GrimoireSMP.getInstance(), "Scroll"), PersistentDataType.STRING) &&
                    item.getItemMeta().getPersistentDataContainer().get(
                                    new NamespacedKey(GrimoireSMP.getInstance(), "Scroll"), PersistentDataType.STRING)
                            .equalsIgnoreCase(data.getScroll().getName())) {
                return true;
            }
        }
        return false;
    }
}
