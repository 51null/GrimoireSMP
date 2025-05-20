package noah.grimoireSMP;

import noah.grimoireSMP.scroll.Scroll;
import noah.grimoireSMP.scroll.ScrollRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PlayerData {

    private static final Map<UUID, PlayerScrollData> playerScrolls = new HashMap<>();

    public static boolean hasScrollData(Player player) {
        return playerScrolls.containsKey(player.getUniqueId());
    }

    public static void assignDefaultScroll(Player player) {
        if (hasScrollData(player)) return;

        ScrollDataManager sdm = ScrollDataManager.getInstance(GrimoireSMP.getInstance());
        UUID uuid = player.getUniqueId();

        // Defaults if we end up assigning a brand‑new random scroll
        Scroll baseScroll    = null;
        int runeEssence      = Scroll.START_RUNE_ESSENCE;
        boolean grimoire     = false;

        // Try loading a saved scroll type
        String savedType = sdm.getScrollType(uuid);
        if (savedType != null) {
            Scroll found = ScrollRegistry.getScroll(savedType);
            if (found != null) {
                // Valid saved scroll
                baseScroll   = found;
                runeEssence  = sdm.getRuneEssence(uuid);
                grimoire     = sdm.getGrimoire(uuid);
            } else {
                // Registry miss—warn and fall back to random
                GrimoireSMP.getInstance().getLogger().warning(
                        "Unknown scroll type '" + savedType + "' for player " + player.getName() +
                                ". Assigning a new random scroll."
                );
            }
        }

        // If we still don't have a scroll (either no savedType, or registry miss)
        if (baseScroll == null) {
            (GrimoireSMP.getInstance()).showScrollRoll(player, chosenKey -> {
                // Once rolled, persist & give
                int startingEssence = Scroll.START_RUNE_ESSENCE;
                sdm.setPlayerData(
                        uuid,
                        chosenKey.toLowerCase(),
                        startingEssence,
                        false);

                Scroll chosen = ScrollRegistry.getScroll(chosenKey).cloneScroll();
                PlayerScrollData data = new PlayerScrollData(chosen, startingEssence);
                data.setGrimoire(false);
                playerScrolls.put(player.getUniqueId(), data);

                // finally give the item
                Bukkit.getScheduler().runTask(GrimoireSMP.getInstance(), () ->
                        player.getInventory().addItem(data.toItemStack(player.getName()))
                );
            });
        }

        // Clone, store in memory, and give to the player
        Scroll scrollInstance = baseScroll.cloneScroll();
        PlayerScrollData data = new PlayerScrollData(scrollInstance, runeEssence);
        data.setGrimoire(grimoire);
        playerScrolls.put(uuid, data);

        // Finally, give the scroll item
        player.getInventory().addItem(data.toItemStack(player.getName()));
    }


    public static PlayerScrollData getPlayerScrollData(Player player) {
        return playerScrolls.get(player.getUniqueId());
    }

    public static void setPlayerScrollData(UUID uuid, PlayerScrollData data) {
        playerScrolls.put(uuid, data);
    }

    // For reset all
    public static java.util.Map<java.util.UUID, PlayerScrollData> getPlayerScrollDataMap() {
        return playerScrolls;
    }



    public static void updateScrollItem(Player player) {
        // Debug message
        if (!hasScrollData(player)) {
            loadPlayerData(player);
        }

        PlayerScrollData data = getPlayerScrollData(player);
        if (data == null) {
            return; // Exit if data remains null after load
        }


        boolean found = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.PAPER && item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().contains(data.getScroll().getName())) {
                player.getInventory().setItem(i, data.toItemStack(player.getName()));
                found = true;
            }
        }
        if (!found) {
            player.getInventory().addItem(data.toItemStack(player.getName()));
            //player.sendMessage("Your soulbound scroll was missing and has been re-added.");
        } else {
            //player.sendMessage("Your soulbound scroll was updated.");
        }
        ScrollDataManager.getInstance(GrimoireSMP.getInstance())
                .setPlayerData(player.getUniqueId(), data.getScroll().getName().toLowerCase().replace(" scroll", ""), data.getRuneEssence(), data.isGrimoire());
    }



    public static void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        ScrollDataManager sdm = ScrollDataManager.getInstance(GrimoireSMP.getInstance());
        //GrimoireSMP.getInstance().getLogger().info("Loading data for player " + player.getName() + " (" + uuid.toString() + ")");
        try {
            String scrollType = sdm.getScrollType(uuid);
            Scroll scroll = null;
            int runeEssence;
            boolean grimoire;
            if (scrollType != null) {
                scroll = ScrollRegistry.getScroll(scrollType);
                if (scroll == null) {
                    // Assign a random scroll if the stored type isn't found.
                    scroll = ScrollRegistry.getRandomScroll();
                } else {
                    scroll = scroll.cloneScroll();
                }
                runeEssence = sdm.getRuneEssence(uuid);
                grimoire = sdm.getGrimoire(uuid);
            } else {
                // No data in the YAML file for this player, so assign a new random scroll.
                (GrimoireSMP.getInstance()).showScrollRoll(player, chosenKey -> {
                    // Once rolled, persist & give
                    int startingEssence = Scroll.START_RUNE_ESSENCE;
                    sdm.setPlayerData(
                            uuid,
                            chosenKey.toLowerCase(),
                            startingEssence,
                            false);

                    Scroll chosen = ScrollRegistry.getScroll(chosenKey).cloneScroll();
                    PlayerScrollData data = new PlayerScrollData(chosen, startingEssence);
                    data.setGrimoire(false);
                    playerScrolls.put(player.getUniqueId(), data);

                    // finally give the item
                    Bukkit.getScheduler().runTask(GrimoireSMP.getInstance(), () ->
                            player.getInventory().addItem(data.toItemStack(player.getName()))
                    );
                });
                runeEssence = Scroll.START_RUNE_ESSENCE;
                grimoire = false;
            }
            PlayerScrollData data = new PlayerScrollData(scroll, runeEssence);
            data.setGrimoire(grimoire);
            playerScrolls.put(uuid, data);
            //player.sendMessage("Player data loaded successfully for " + player.getName());
            // Add the scroll item to the player's inventory.
            player.getInventory().addItem(data.toItemStack(player.getName()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void clearPlayerData() {

        playerScrolls.clear();
    }

}
