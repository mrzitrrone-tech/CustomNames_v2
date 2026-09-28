package mrzitrrone.listeners;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.gui.CustomNameGUI;
import mrzitrrone.gui.CustomNameHolder;
import mrzitrrone.gui.MenuAction;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.util.SchedulerUtil;
import mrzitrrone.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIListener implements Listener {

    /** Art der erwarteten Chat-Eingabe. */
    public enum InputType {
        NAME,
        PREFIX
    }

    private final CustomNamePlugin plugin;
    private final ConfigManager config;
    private final Map<UUID, InputType> awaitingInput = new ConcurrentHashMap<>();

    public GUIListener(CustomNamePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CustomNameHolder holder)) {
            return;
        }
        // Immer canceln – auch Shift-Klicks aus dem eigenen Inventar
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof CustomNameHolder)) {
            return;
        }

        MenuAction action = holder.getAction(event.getSlot());
        if (action == null || action.type() == MenuAction.Type.NONE) {
            return;
        }

        switch (action.type()) {
            case OPEN_COLORS -> new CustomNameGUI(plugin, player).openColorMenu();

            case BACK -> new CustomNameGUI(plugin, player).open();

            case CLOSE -> {
                player.closeInventory();
                if (config.hasMessage("gui-closed")) {
                    player.sendMessage(config.msg("gui-closed"));
                }
            }

            case INPUT_NAME -> {
                player.closeInventory();
                awaitingInput.put(player.getUniqueId(), InputType.NAME);
                player.sendMessage(config.msg("enter-minimessage"));
                player.sendMessage(config.msgRaw("enter-minimessage-example"));
                player.sendMessage(config.msgRaw("input-cancel-hint"));
            }

            case INPUT_PREFIX -> {
                if (!player.hasPermission("customname.prefix.use")
                        && !player.hasPermission("customname.admin")) {
                    player.sendMessage(config.msg("no-permission-prefix"));
                    return;
                }
                player.closeInventory();
                awaitingInput.put(player.getUniqueId(), InputType.PREFIX);
                player.sendMessage(config.msg("enter-prefix"));
                player.sendMessage(config.msgRaw("enter-prefix-example"));
                player.sendMessage(config.msgRaw("input-cancel-hint"));
            }

            case RESET_NAME -> {
                plugin.getNameManager().removeCustomName(player);
                player.sendMessage(config.msg("name-reset"));
                reopenOrClose(player);
            }

            case RESET_PREFIX -> {
                plugin.getPrefixManager().removePrefix(player.getUniqueId());
                player.sendMessage(config.msg("prefix-reset"));
                reopenOrClose(player);
            }

            case SELECT_COLOR -> {
                String format = action.value();
                if (format == null || !Text.isValid(format)) {
                    player.sendMessage(config.msg("invalid-format"));
                    return;
                }
                plugin.getNameManager().setCustomName(player, format, false);
                player.sendMessage(config.msg("name-changed",
                        Text.placeholder("name", Text.parse(format))));
                reopenOrClose(player);
            }

            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CustomNameHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        awaitingInput.remove(event.getPlayer().getUniqueId());
    }

    private void reopenOrClose(Player player) {
        if (plugin.getConfig().getBoolean("gui.close-after-action", true)) {
            player.closeInventory();
        } else {
            SchedulerUtil.runForPlayerLater(plugin, player,
                    () -> new CustomNameGUI(plugin, player).open(), 1L);
        }
    }

    // ===== Chat-Eingabe =====
    public boolean isAwaitingInput(UUID uuid) {
        return awaitingInput.containsKey(uuid);
    }

    public InputType getInputType(UUID uuid) {
        return awaitingInput.get(uuid);
    }

    public void clearInput(UUID uuid) {
        awaitingInput.remove(uuid);
    }

    public void setInput(UUID uuid, InputType type) {
        if (type == null) {
            awaitingInput.remove(uuid);
        } else {
            awaitingInput.put(uuid, type);
        }
    }
}
