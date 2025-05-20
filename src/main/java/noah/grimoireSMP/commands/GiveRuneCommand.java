package noah.grimoireSMP.commands;

import noah.grimoireSMP.Constants;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

public class GiveRuneCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("grimoire.giverune")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /giverune <player>");
            return true;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        ItemStack rune = new ItemStack(Material.COAL);
        ItemMeta meta = rune.getItemMeta();
        meta.setDisplayName("§1Rune");
        meta.setCustomModelData(Constants.RUNE_MODEL_DATA);
        rune.setItemMeta(meta);
        target.getInventory().addItem(rune);
        sender.sendMessage(ChatColor.GREEN + "Rune given to " + target.getName());
        target.sendMessage(ChatColor.GREEN + "You have received a Rune!");
        return true;
    }
}
