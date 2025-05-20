package noah.grimoireSMP.listener;

import noah.grimoireSMP.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!PlayerData.hasScrollData(player)) {
            PlayerData.assignDefaultScroll(player);
        }
    }
}
