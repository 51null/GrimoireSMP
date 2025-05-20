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

public class GiveHandleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("grimoire.givehandle")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givehandle <player>");
            return true;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        ItemStack handle = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = handle.getItemMeta();
        meta.setDisplayName("§6Grimoire Case");
        meta.setCustomModelData(Constants.GRIMOIRE_HANDLE_MODEL_DATA);
        handle.setItemMeta(meta);
        target.getInventory().addItem(handle);
        sender.sendMessage(ChatColor.GREEN + "Grimoire Case given to " + target.getName());
        target.sendMessage(ChatColor.GREEN + "You have received a Grimoire Case!");
        return true;
    }
}
