package mrzitrrone.manager;

import mrzitrrone.CustomNamePlugin;
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
                e.printStackTrace();
            }
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String prefix = messages.getString("prefix", "&6[CustomName] &7");
        String message = messages.getString("messages." + path, "&cMessage not found: " + path);
        return colorize(prefix + message);
    }

    public String getMessageWithoutPrefix(String path) {
        return colorize(messages.getString("messages." + path, "&cMessage not found: " + path));
    }

    public String colorize(String text) {
        if (text == null) return "";
        return text.replace('&', '§');
    }

    public FileConfiguration getConfig() {
        return config;
    }
}