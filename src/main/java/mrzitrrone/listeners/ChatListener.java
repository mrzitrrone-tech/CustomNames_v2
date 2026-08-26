package mrzitrrone.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import mrzitrrone.CustomNamePlugin;
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

        if (!guiListener.isAwaitingInput(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        guiListener.setAwaitingInput(player.getUniqueId(), false);

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("abbrechen")) {
            player.sendMessage(plugin.getConfigManager().getMessage("chat-cancelled"));
            return;
        }

        player.getScheduler().run(plugin, (task) -> {
            player.performCommand("customname " + message);
        }, null);
    }
}