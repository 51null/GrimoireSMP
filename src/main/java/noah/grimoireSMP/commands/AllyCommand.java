package noah.grimoireSMP.commands;

import noah.grimoireSMP.AllyManager;
import noah.grimoireSMP.GrimoireSMP;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AllyCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can manage alliances.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("§cUsage: /ally <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }
        UUID you  = player.getUniqueId();
        UUID them = target.getUniqueId();

        if (you.equals(them)) {
            player.sendMessage("§cYou cannot ally with yourself.");
            return true;
        }

        // 1) If already allies → remove
        if (AllyManager.isAlly(you, them)) {
            AllyManager.removeAlly(you, them);
            player.sendMessage("§eYou are no longer allied with §6" + target.getName());
            return true;
        }

        // 2) If THEY sent you a request → accept
        if (AllyManager.hasPendingRequest(them, you)) {
            AllyManager.clearPendingRequest(them, you);
            AllyManager.addAlly(you, them);
            player.sendMessage("§aAlliance formed with §6" + target.getName() + "§a!");
            if (target.isOnline()) {
                ((Player)target).sendMessage(
                        "§aYour alliance request was accepted by §6" + player.getName() + "§a."
                );
            }
            return true;
        }

        // 3) If YOU already sent them a request → remind
        if (AllyManager.hasPendingRequest(you, them)) {
            player.sendMessage("§eYou have already sent an alliance request to §6"
                    + target.getName() + "§e. Waiting for them to accept.");
            return true;
        }

        // 4) Otherwise → send new request
        AllyManager.addPendingRequest(you, them);
        player.sendMessage("§aAlliance request sent to §6" + target.getName() + "§a.");
        if (target.isOnline()) {
            ((Player)target).sendMessage(
                    "§6" + player.getName() + " §ahas requested to ally with you. §7" +
                            "Type §a/ally " + player.getName() + "§7 to accept."
            );
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .toList();
        }
        return java.util.Collections.emptyList();
    }
}
