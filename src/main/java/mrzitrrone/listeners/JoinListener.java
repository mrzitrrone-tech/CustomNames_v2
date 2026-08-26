package mrzitrrone.listeners;

import mrzitrrone.CustomNamePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final CustomNamePlugin plugin;

    public JoinListener(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getNameManager().hasCustomName(player.getUniqueId())) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
                plugin.getNameManager().updatePlayerDisplayName(player);
            }, 5L);
        }
    }
}