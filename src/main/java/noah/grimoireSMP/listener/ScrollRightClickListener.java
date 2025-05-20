package noah.grimoireSMP.listener;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.Action;

public class ScrollRightClickListener implements Listener {

    private final JavaPlugin plugin;
    // Use a NamespacedKey; consider centralizing keys in a Constants class.
    private final NamespacedKey scrollKey;

    public ScrollRightClickListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scrollKey = new NamespacedKey(plugin, "Scroll");
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();

        // Check if either hand is holding a scroll
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean holdingScroll = false;

        // Create your key (it should be the same used in your scroll tagging)
        NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");

        if (isScroll(mainHand, scrollKey)) {
            holdingScroll = true;
        } else if (isScroll(offHand, scrollKey)) {
            holdingScroll = true;
        }
        if (holdingScroll) {
            // Retrieve the player's stored scroll data from PlayerData.
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data != null) {
                // Call the active ability (for example, ability number 1)
                data.getScroll().useActiveAbility(1, player);
            }
        }
    }

    @EventHandler
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        // Check if either hand is holding a scroll
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean holdingScroll = false;

        // Create your key (it should be the same used in your scroll tagging)
        NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");

        if (isScroll(mainHand, scrollKey)) {
            holdingScroll = true;
        } else if (isScroll(offHand, scrollKey)) {
            holdingScroll = true;
        }
        if (holdingScroll) {
            // Retrieve the player's stored scroll data from PlayerData.
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data != null) {
                // Call the active ability (for example, ability number 1)
                data.getScroll().useGrimoireAbility(player);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack dropped = event.getItemDrop().getItemStack();


        boolean holdingScroll = false;

        // Create your key (it should be the same used in your scroll tagging)
        NamespacedKey scrollKey = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");

        if (isScroll(dropped, scrollKey)) {
            holdingScroll = true;
        }
        if (holdingScroll) {
            // Retrieve the player's stored scroll data from PlayerData.
            PlayerScrollData data = PlayerData.getPlayerScrollData(player);
            if (data != null) {
                // Call the active ability (for example, ability number 1)
                data.getScroll().useActiveAbility(2, player);
            }
        }
    }

    private boolean isScroll(ItemStack item, NamespacedKey scrollKey) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(scrollKey, PersistentDataType.STRING);
    }
}
