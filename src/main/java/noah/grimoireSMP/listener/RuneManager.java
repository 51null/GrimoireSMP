package noah.grimoireSMP.listener;

import noah.grimoireSMP.Constants;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.Scroll;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RuneManager implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 1) Prevent scroll drop
        event.getDrops().removeIf(item ->
                item.getType() == Material.PAPER && item.hasItemMeta()
        );

        Player victim = event.getEntity();
        PlayerScrollData victimData = PlayerData.getPlayerScrollData(victim);

        // Save pre-death Essence
        int victimEssenceBefore = (victimData != null)
                ? victimData.getRuneEssence()
                : 0;

        // 2) Reduce victim Essence if they had any
        if (victimEssenceBefore > 0) {
            victimData.reduceRuneEssence(1);
            victim.sendMessage("§b§oYou lost 1 Rune Essence! Your scroll is now §r"
                    + getScrollStatus(victimData.getRuneEssence()));
            PlayerData.updateScrollItem(victim);
        }

        Player killer = victim.getKiller();
        if (killer != null && victimEssenceBefore > 0) {
            // Only consider transferring if the victim actually lost Essence
            PlayerScrollData killerData = PlayerData.getPlayerScrollData(killer);
            int killerEssenceBefore = (killerData != null)
                    ? killerData.getRuneEssence()
                    : 0;

            if (killerData != null) {
                if (killerEssenceBefore > 0) {
                    // Killer has a non-lost scroll → normal transfer
                    if (killerEssenceBefore < Scroll.MAX_RUNE_ESSENCE) {
                        killerData.addRuneEssence(1);
                        killer.sendMessage("§b§oYou gained 1 Rune Essence! Your scroll is now §r"
                                + getScrollStatus(killerData.getRuneEssence()));
                        PlayerData.updateScrollItem(killer);
                    } else {
                        // At cap → drop a Rune
                        dropRune(victim);
                    }
                } else {
                    // Killer had a Lost scroll (0 essence) → only drop a Rune
                    dropRune(victim);
                }
            }
        } else if (killer == null && victimEssenceBefore > 0) {
            // Natural death and victim had Essence → drop a Rune
            dropRune(victim);
        }

        // 3) Grimoire reversion always happens, independent of Essence
        if (victimData != null && victimData.isGrimoire()) {
            victimData.setGrimoire(false);
            PlayerData.updateScrollItem(victim);
            dropGrimoireHandle(victim);
            victim.sendMessage("§b§oYour Grimoire reverted to a Scroll and dropped a Grimoire Case.");
        }
    }



    private void dropRune(Player victim) {
        ItemStack rune = new ItemStack(Material.COAL);
        ItemMeta meta = rune.getItemMeta();
        meta.setDisplayName("§1Rune");
        meta.setCustomModelData(Constants.RUNE_MODEL_DATA);
        rune.setItemMeta(meta);
        victim.getWorld().dropItemNaturally(victim.getLocation(), rune);
        //victim.sendMessage("§b§oA Rune has been dropped at your death location!");
    }

    private void dropGrimoireHandle(Player victim) {
        ItemStack handle = new ItemStack(Material.BLAZE_ROD);
        org.bukkit.inventory.meta.ItemMeta meta = handle.getItemMeta();
        meta.setDisplayName("§6Grimoire Case");
        meta.setCustomModelData(Constants.GRIMOIRE_HANDLE_MODEL_DATA);
        handle.setItemMeta(meta);
        victim.getWorld().dropItemNaturally(victim.getLocation(), handle);
    }

    public String getScrollStatus(int essence) {
        return switch (essence) {
            case 0 -> "§f§lLOST";
            case 1 -> "§7§lFRACTURED";
            case 2 -> "§7§lFADED";
            case 3 -> "§a§lPOTENT";
            case 4 -> "§c§lEMPOWERED";
            case 5 -> "§e§lGODLY";
            case 6 -> "§e§lGODLY + 1";
            case 7 -> "§e§lGODLY + 2";
            default -> "Unknown";
        };
    }

}
