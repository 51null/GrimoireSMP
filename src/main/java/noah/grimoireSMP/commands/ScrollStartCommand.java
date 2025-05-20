package noah.grimoireSMP.commands;

import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ScrollStartCommand implements CommandExecutor {
    private final GrimoireSMP plugin;

    public ScrollStartCommand(GrimoireSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("grimoire.scrollstart")) {
            sender.sendMessage("§cYou must be an operator to do that.");
            return true;
        }

        boolean enabled = plugin.getConfig().getBoolean("scroll-system-enabled", false);
        if (enabled) {
            sender.sendMessage("§eScroll system is already enabled.");
            return true;
        }

        plugin.getConfig().set("scroll-system-enabled", true);
        plugin.saveConfig();

        sender.sendMessage("§aScroll system ENABLED! Registering all scroll handlers…");
        plugin.startScrollSystem();

        return true;
    }
}
