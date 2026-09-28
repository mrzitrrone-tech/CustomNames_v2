package mrzitrrone.manager;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private final CustomNamePlugin plugin;

    private FileConfiguration config;
    private FileConfiguration messages;
    private File messagesFile;

    public ConfigManager(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                InputStream in = plugin.getResource("messages.yml");
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    messagesFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte messages.yml nicht erstellen: " + e.getMessage());
            }
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /** Nachricht inkl. Prefix als Component (MiniMessage). */
    public Component msg(String path, TagResolver... resolvers) {
        String prefix = messages.getString("prefix", "");
        String message = messages.getString("messages." + path, "<red>Message not found: " + path);
        return Text.parse(prefix + message, resolvers);
    }

    /** Nachricht ohne Prefix als Component (MiniMessage). */
    public Component msgRaw(String path, TagResolver... resolvers) {
        String message = messages.getString("messages." + path, "<red>Message not found: " + path);
        return Text.parse(message, resolvers);
    }

    public boolean hasMessage(String path) {
        return messages.contains("messages." + path);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}
