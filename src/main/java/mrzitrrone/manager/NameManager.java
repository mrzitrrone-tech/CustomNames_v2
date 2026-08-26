package mrzitrrone.manager;

import mrzitrrone.CustomNamePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameManager {

    private final CustomNamePlugin plugin;
    private final Map<UUID, String> customNames = new HashMap<>();
    private final Map<UUID, Boolean> isNicked = new HashMap<>();

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

        player.displayName(Component.text(player.getName()));
        player.playerListName(Component.text(player.getName()));

        plugin.getNamesConfig().set(player.getUniqueId().toString(), null);
        plugin.saveNamesConfig();
    }

    public void updatePlayerDisplayName(Player player) {
        String customName = customNames.get(player.getUniqueId());
        if (customName == null) {
            player.displayName(Component.text(player.getName()));
            player.playerListName(Component.text(player.getName()));
            return;
        }

        try {
            Component nameComponent = plugin.getMiniMessage().deserialize(customName);

            if (isNicked.getOrDefault(player.getUniqueId(), false)
                    && !player.hasPermission("customname.bypass.prefix")) {
                String prefix = plugin.getConfig().getString("nick-prefix", "~");
                nameComponent = Component.text(prefix).append(nameComponent);
            }

            player.displayName(nameComponent);
            player.playerListName(nameComponent);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Setzen des Custom Names für " + player.getName());
            e.printStackTrace();
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

    public String getDisplayName(Player player) {
        if (!hasCustomName(player.getUniqueId())) {
            return player.getName();
        }

        String customName = customNames.get(player.getUniqueId());
        try {
            Component component = plugin.getMiniMessage().deserialize(customName);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            return player.getName();
        }
    }

    public String stripMiniMessage(String text) {
        try {
            Component component = plugin.getMiniMessage().deserialize(text);
            return PlainTextComponentSerializer.plainText().serialize(component);
        } catch (Exception e) {
            return text;
        }
    }
}