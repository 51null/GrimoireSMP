package noah.grimoireSMP.commands;

import noah.grimoireSMP.GrimoireSMP;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.ScrollDataManager;
import noah.grimoireSMP.PlayerScrollData;
import noah.grimoireSMP.scroll.ScrollRegistry;
import noah.grimoireSMP.scroll.Scroll;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GiveScrollCommand implements CommandExecutor, TabCompleter {
    private final GrimoireSMP plugin;

    public GiveScrollCommand(GrimoireSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("grimoire.givescroll")) {
            sender.sendMessage("§cYou don’t have permission to do that.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /givescroll <scroll> <player>");
            return true;
        }

        String scrollKey = args[0].toLowerCase();
        Scroll template = ScrollRegistry.getScroll(scrollKey);
        if (template == null) {
            sender.sendMessage("§cUnknown scroll type. Available: " +
                    String.join(", ", ScrollRegistry.getAllScrollNames()));
            return true;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(args[1]);
        if (offline == null || (!offline.hasPlayedBefore() && !offline.isOnline())) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }
        UUID uuid = offline.getUniqueId();

        // Persist to disk
        ScrollDataManager sdm = ScrollDataManager.getInstance(plugin);
        int defaultRune = Scroll.START_RUNE_ESSENCE;
        boolean grimoire = false;
        sdm.setPlayerData(uuid, template.getName().toLowerCase(), defaultRune, grimoire);

        // Load into runtime memory & give item if online
        if (offline.isOnline()) {
            Player pl = offline.getPlayer();

            // Reload into memory
            PlayerData.loadPlayerData(pl);

            // Overwrite scroll in memory
            PlayerScrollData psd = new PlayerScrollData(template.cloneScroll(),
                    Scroll.START_RUNE_ESSENCE);
            psd.setGrimoire(false);
            PlayerData.setPlayerScrollData(uuid, psd);

            // Remove any old scroll items
            NamespacedKey sk = new NamespacedKey(plugin, "Scroll");
            for (ItemStack item : pl.getInventory().getContents()) {
                if (item != null
                        && item.getType() == Material.PAPER
                        && item.hasItemMeta()
                        && item.getItemMeta()
                        .getPersistentDataContainer()
                        .has(sk, PersistentDataType.STRING)) {
                    pl.getInventory().remove(item);
                }
            }

            // Give fresh one
            PlayerData.updateScrollItem(pl);

            pl.sendMessage("§aYou have been given the “" +
                    template.getName() + "” scroll!");
        }

        sender.sendMessage("§aScroll “" + template.getName() +
                "” assigned to " + offline.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      Command cmd,
                                      String alias,
                                      String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return ScrollRegistry.getAllScrollNames().stream()
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
