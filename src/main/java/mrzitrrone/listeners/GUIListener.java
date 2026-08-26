package mrzitrrone.listeners;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.gui.CustomNameGUI;
import mrzitrrone.manager.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final CustomNamePlugin plugin;
    private final ConfigManager config;
    private final Map<UUID, Boolean> awaitingInput = new HashMap<>();

    public GUIListener(CustomNamePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String mainTitle = config.colorize(plugin.getConfig().getString("gui.title"));
        String presetTitle = config.colorize("&7Farb-Presets");

        String title = event.getView().getTitle();

        // ✅ Wenn es unser GUI ist → IMMER canceln
        if (title.equals(mainTitle) || title.equals(presetTitle)) {
            event.setCancelled(true);
        } else {
            return;
        }

        if (event.getCurrentItem() == null) return;

        int slot = event.getSlot();

        // ===== HAUPTMENÜ =====
        if (title.equals(mainTitle)) {

            if (slot == 11) {
                new CustomNameGUI(plugin, player).openPresetMenu();
            }

            else if (slot == 13) {
                player.closeInventory();
                player.sendMessage(config.getMessage("enter-minimessage"));
                player.sendMessage(config.getMessageWithoutPrefix("enter-minimessage-example"));
                awaitingInput.put(player.getUniqueId(), true);
            }

            else if (slot == 15) {
                plugin.getNameManager().removeCustomName(player);
                player.sendMessage(config.getMessage("name-reset"));
                player.closeInventory();
            }
        }

        // ===== PRESET MENÜ =====
        else if (title.equals(presetTitle)) {

            if (slot == 26) {
                new CustomNameGUI(plugin, player).open();
                return;
            }

            if (slot < 15) {
                ItemStack item = event.getCurrentItem();
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    String colorCode = displayName.length() >= 2 ? displayName.substring(0, 2) : "§f";
                    String formattedName = colorCode.replace('§', '&') + player.getName();

                    plugin.getNameManager().setCustomName(player, formattedName, false);
                    player.sendMessage(config.getMessage("name-changed"));
                    player.closeInventory();
                }
            }
        }
    }

    public boolean isAwaitingInput(UUID uuid) {
        return awaitingInput.getOrDefault(uuid, false);
    }

    public void setAwaitingInput(UUID uuid, boolean value) {
        if (value) awaitingInput.put(uuid, true);
        else awaitingInput.remove(uuid);
    }
}