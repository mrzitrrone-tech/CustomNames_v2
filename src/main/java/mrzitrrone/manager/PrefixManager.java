package mrzitrrone.manager;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.SchedulerUtil;
import mrzitrrone.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet die Prefixe der Spieler (voller MiniMessage Support).
 */
public class PrefixManager {

    private final CustomNamePlugin plugin;
    private final Map<UUID, String> prefixes = new ConcurrentHashMap<>();

    private File prefixFile;
    private FileConfiguration prefixConfig;

    public PrefixManager(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        prefixFile = new File(plugin.getDataFolder(), "prefixes.yml");
        if (!prefixFile.exists()) {
            try {
                prefixFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte prefixes.yml nicht erstellen: " + e.getMessage());
            }
        }
        prefixConfig = YamlConfiguration.loadConfiguration(prefixFile);

        prefixes.clear();
        for (String key : prefixConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String prefix = prefixConfig.getString(key + ".prefix");
                if (prefix != null && !prefix.isEmpty()) {
                    prefixes.put(uuid, prefix);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ungültige UUID in prefixes.yml: " + key);
            }
        }
    }

    /** Speichert asynchron (Folia-sicher). */
    public void save() {
        saveInternal(true);
    }

    /** Speichert sofort im aktuellen Thread (z.B. onDisable). */
    public void saveSync() {
        saveInternal(false);
    }

    private void saveInternal(boolean async) {
        if (prefixConfig == null || prefixFile == null) {
            return;
        }
        for (String key : prefixConfig.getKeys(false)) {
            prefixConfig.set(key, null);
        }
        for (Map.Entry<UUID, String> entry : prefixes.entrySet()) {
            prefixConfig.set(entry.getKey() + ".prefix", entry.getValue());
        }

        Runnable writer = () -> {
            try {
                prefixConfig.save(prefixFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte prefixes.yml nicht speichern: " + e.getMessage());
            }
        };

        if (async) {
            SchedulerUtil.runAsync(plugin, writer);
        } else {
            writer.run();
        }
    }

    public void setPrefix(UUID uuid, String miniMessagePrefix) {
        if (miniMessagePrefix == null || miniMessagePrefix.isBlank()) {
            removePrefix(uuid);
            return;
        }
        prefixes.put(uuid, miniMessagePrefix);
        refresh(uuid);
        save();
    }

    public void removePrefix(UUID uuid) {
        prefixes.remove(uuid);
        refresh(uuid);
        save();
    }

    private void refresh(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            SchedulerUtil.runForPlayer(plugin, player,
                    () -> plugin.getNameManager().updatePlayerDisplayName(player));
        }
    }

    /** Roher MiniMessage String oder {@code null}. */
    public String getRawPrefix(UUID uuid) {
        String prefix = prefixes.get(uuid);
        if (prefix == null || prefix.isEmpty()) {
            String def = plugin.getConfig().getString("prefix.default", "");
            return (def == null || def.isEmpty()) ? null : def;
        }
        return prefix;
    }

    public boolean hasPrefix(UUID uuid) {
        return getRawPrefix(uuid) != null;
    }

    /** Prefix als Component inkl. konfiguriertem Trennzeichen. */
    public Component getPrefixComponent(UUID uuid) {
        String raw = getRawPrefix(uuid);
        if (raw == null) {
            return Component.empty();
        }
        String separator = plugin.getConfig().getString("prefix.separator", " ");
        try {
            Component component = Text.parse(raw);
            if (separator != null && !separator.isEmpty()) {
                component = component.append(Text.parse(separator));
            }
            return component;
        } catch (Exception e) {
            plugin.getLogger().warning("Ungültiges Prefix-Format für " + uuid + ": " + raw);
            return Component.empty();
        }
    }

    public int getMaxLength() {
        return plugin.getConfig().getInt("prefix.max-length", 24);
    }

    public Map<UUID, String> getPrefixes() {
        return prefixes;
    }
}
