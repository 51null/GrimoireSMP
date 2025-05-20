package noah.grimoireSMP.commands;

import noah.grimoireSMP.Constants;
import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GiveSwitchCaseCommand implements CommandExecutor, TabCompleter {
    private final NamespacedKey scKey =
            new NamespacedKey(GrimoireSMP.getInstance(), "switch_case");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("grimoire.switchcase.give")) {
            sender.sendMessage("§cYou don’t have permission to do that.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /giveswitchcase <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }

        // Build the Switch Case item
        ItemStack switchCase = new ItemStack(Material.PAPER);
        ItemMeta meta = switchCase.getItemMeta();
        meta.setDisplayName("§dSwitch Case");
        meta.setCustomModelData(Constants.SWITCH_CASE_MODEL_DATA);
        meta.getPersistentDataContainer().set(scKey, PersistentDataType.BYTE, (byte)1);
        switchCase.setItemMeta(meta);

        target.getInventory().addItem(switchCase);
        sender.sendMessage("§aGave a Switch Case to §6" + target.getName());
        target.sendMessage("§aYou received a §dSwitch Case§a!");

        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .toList();
        }
        return java.util.Collections.emptyList();
    }
}
