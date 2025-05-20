package noah.grimoireSMP.commands;

import noah.grimoireSMP.ScrollDataManager;
import noah.grimoireSMP.PlayerData;
import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class ResetAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only ops or console can run this command.
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("grimoire.resetall")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        // Clear in-memory data.
        PlayerData.getPlayerScrollDataMap().clear();

        // Clear file storage.
        ScrollDataManager sdm = ScrollDataManager.getInstance(GrimoireSMP.getInstance());
        sdm.getConfig().set("players", null);
        sdm.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "All scroll data has been reset. Players will receive new scrolls on their next join.");
        return true;
    }
}