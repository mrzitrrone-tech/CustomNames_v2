package mrzitrrone.listeners;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.SchedulerUtil;
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

        boolean hasName = plugin.getNameManager().hasCustomName(player.getUniqueId());
        boolean hasPrefix = plugin.getPrefixManager().hasPrefix(player.getUniqueId());

        if (!hasName && !hasPrefix) {
            return;
        }

        // Folia-sicher: im Kontext des Spielers, leicht verzögert
        SchedulerUtil.runForPlayerLater(plugin, player,
                () -> plugin.getNameManager().updatePlayerDisplayName(player), 5L);
    }
}
