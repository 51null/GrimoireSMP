// SwitchCaseListener.java
package noah.grimoireSMP.listener;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.ScrollDataManager;
import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class SwitchCaseListener implements Listener {
    private final NamespacedKey scKey =
            new NamespacedKey(GrimoireSMP.getInstance(), "switch_case");

    @EventHandler
    public void onUse(PlayerInteractEvent ev) {
        if (ev.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(scKey, PersistentDataType.BYTE))
            return;

        ev.setCancelled(true);
        Player p = ev.getPlayer();
        if (PlayerData.getPlayerScrollData(p).getRuneEssence() <= 0){
            p.sendMessage("§b§oYour scroll is §r§f§lLOST§r§b§o. "
                    + "You cannot use runes until you perform the Ritual.");
            return;
        }
        // consume one
        if (item.getAmount() > 1) item.setAmount(item.getAmount()-1);
        else p.getInventory().remove(item);

        // roll for a brand new scroll
        GrimoireSMP.getInstance().showScrollRoll(p, chosenKey -> {
            UUID uuid = p.getUniqueId();
            Scroll template = ScrollRegistry.getScroll(chosenKey);

            ScrollDataManager sdm = ScrollDataManager.getInstance(GrimoireSMP.getInstance());
            int defaultRune = Scroll.START_RUNE_ESSENCE;
            boolean grimoire = false;
            sdm.setPlayerData(uuid, template.getName().toLowerCase(), defaultRune, grimoire);

            // Load into runtime memory & give item if online


            // Reload into memory
            PlayerData.loadPlayerData(p);

            // Overwrite scroll in memory
            PlayerScrollData psd = new PlayerScrollData(template.cloneScroll(),
                    Scroll.START_RUNE_ESSENCE);
            psd.setGrimoire(false);
            PlayerData.setPlayerScrollData(uuid, psd);

            // Remove any old scroll items
            NamespacedKey sk = new NamespacedKey(GrimoireSMP.getInstance(), "Scroll");
            for (ItemStack itemS : p.getInventory().getContents()) {
                if (itemS != null
                        && itemS.getType() == Material.PAPER
                        && itemS.hasItemMeta()
                        && itemS.getItemMeta()
                        .getPersistentDataContainer()
                        .has(sk, PersistentDataType.STRING)) {
                    p.getInventory().remove(itemS);
                }
            }

            // Give fresh one
            PlayerData.updateScrollItem(p);
        });
    }
}
