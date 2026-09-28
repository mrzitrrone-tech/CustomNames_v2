package mrzitrrone.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.SchedulerUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final CustomNamePlugin plugin;
    private final GUIListener guiListener;

    public ChatListener(CustomNamePlugin plugin, GUIListener guiListener) {
        this.plugin = plugin;
        this.guiListener = guiListener;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        GUIListener.InputType type = guiListener.getInputType(player.getUniqueId());
        if (type == null) {
            return;
        }

        event.setCancelled(true);
        guiListener.clearInput(player.getUniqueId());

        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("abbrechen")) {
            player.sendMessage(plugin.getConfigManager().msg("chat-cancelled"));
            return;
        }

        final String command = switch (type) {
            case PREFIX -> "prefix " + message;
            case NAME -> "customname " + message;
        };

        // Chat ist async – Ausführung Folia-sicher im Kontext des Spielers
        SchedulerUtil.runForPlayer(plugin, player, () -> player.performCommand(command));
    }
}
