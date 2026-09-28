package mrzitrrone.manager;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.SchedulerUtil;
import mrzitrrone.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NameManager {

    private final CustomNamePlugin plugin;
    private final Map<UUID, String> customNames = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> isNicked = new ConcurrentHashMap<>();

    public NameManager(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadNames() {
        customNames.clear();
        isNicked.clear();

        for (String key : plugin.getNamesConfig().getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String customName = plugin.getNamesConfig().getString(key + ".name");
                boolean nicked = plugin.getNamesConfig().getBoolean(key + ".nicked", false);
                if (customName != null) {
                    customNames.put(uuid, customName);
                    isNicked.put(uuid, nicked);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ungültige UUID in names.yml: " + key);
            }
        }
    }

    public void saveNames() {
        for (Map.Entry<UUID, String> entry : customNames.entrySet()) {
            String path = entry.getKey().toString();
            plugin.getNamesConfig().set(path + ".name", entry.getValue());
            plugin.getNamesConfig().set(path + ".nicked", isNicked.getOrDefault(entry.getKey(), false));
        }
        SchedulerUtil.runAsync(plugin, plugin::saveNamesConfig);
    }

    public void saveNamesSync() {
        for (Map.Entry<UUID, String> entry : customNames.entrySet()) {
            String path = entry.getKey().toString();
            plugin.getNamesConfig().set(path + ".name", entry.getValue());
            plugin.getNamesConfig().set(path + ".nicked", isNicked.getOrDefault(entry.getKey(), false));
        }
        plugin.saveNamesConfig();
    }

    public void setCustomName(Player player, String miniMessageFormat, boolean isNick) {
        customNames.put(player.getUniqueId(), miniMessageFormat);
        isNicked.put(player.getUniqueId(), isNick);
        updatePlayerDisplayName(player);
        saveNames();
    }

    public void removeCustomName(Player player) {
        customNames.remove(player.getUniqueId());
        isNicked.remove(player.getUniqueId());

        plugin.getNamesConfig().set(player.getUniqueId().toString(), null);
        SchedulerUtil.runAsync(plugin, plugin::saveNamesConfig);

        updatePlayerDisplayName(player);
    }

    /**
     * Baut den kompletten Anzeigenamen: Prefix + (Nick-Prefix) + Name.
     */
    public Component buildDisplayName(Player player) {
        Component nameComponent;
        String customName = customNames.get(player.getUniqueId());

        if (customName == null) {
            nameComponent = Component.text(player.getName());
        } else {
            try {
                nameComponent = Text.parse(customName);
            } catch (Exception e) {
                plugin.getLogger().warning("Ungültiger CustomName für " + player.getName() + ": " + customName);
                nameComponent = Component.text(player.getName());
            }

            if (isNicked.getOrDefault(player.getUniqueId(), false)
                    && !player.hasPermission("customname.bypass.prefix")) {
                String nickPrefix = plugin.getConfig().getString("nick-prefix", "~");
                nameComponent = Text.parse(nickPrefix).append(nameComponent);
            }
        }

        if (plugin.getConfig().getBoolean("prefix.enabled", true)
                && plugin.getPrefixManager() != null) {
            Component prefix = plugin.getPrefixManager().getPrefixComponent(player.getUniqueId());
            if (!Text.plain(prefix).isEmpty()) {
                nameComponent = prefix.append(nameComponent);
            }
        }

        return nameComponent;
    }

    public void updatePlayerDisplayName(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        try {
            Component display = buildDisplayName(player);
            player.displayName(display);
            if (plugin.getConfig().getBoolean("update-tablist", true)) {
                player.playerListName(display);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Setzen des Anzeigenamens für " + player.getName()
                    + ": " + e.getMessage());
        }
    }

    public String getCustomName(UUID uuid) {
        return customNames.get(uuid);
    }

    public boolean hasCustomName(UUID uuid) {
        return customNames.containsKey(uuid);
    }

    public boolean isNicked(UUID uuid) {
        return isNicked.getOrDefault(uuid, false);
    }

    /** Anzeigename in Legacy-Form (z.B. für PlaceholderAPI). */
    public String getDisplayName(Player player) {
        try {
            return Text.legacy(buildDisplayName(player));
        } catch (Exception e) {
            return player.getName();
        }
    }

    /** Nur der Name (ohne Prefix) in Legacy-Form. */
    public String getNameOnly(Player player) {
        String customName = customNames.get(player.getUniqueId());
        if (customName == null) {
            return player.getName();
        }
        try {
            return Text.legacy(Text.parse(customName));
        } catch (Exception e) {
            return player.getName();
        }
    }

    public String stripMiniMessage(String text) {
        return Text.plain(text);
    }
}
