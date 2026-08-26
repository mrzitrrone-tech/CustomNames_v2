package mrzitrrone;

import mrzitrrone.listeners.ChatListener;
import mrzitrrone.listeners.GUIListener;
import mrzitrrone.listeners.JoinListener;
import mrzitrrone.commands.CustomNameCommand;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.manager.NameManager;
import mrzitrrone.placeholder.CustomNamePlaceholder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomNamePlugin extends JavaPlugin {

    private static CustomNamePlugin instance;
    private NameManager nameManager;
    private ConfigManager configManager;
    private MiniMessage miniMessage;
    private File namesFile;
    private FileConfiguration namesConfig;
    private GUIListener guiListener;

    @Override
    public void onEnable() {
        instance = this;
        miniMessage = MiniMessage.miniMessage();

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        setupNamesFile();

        nameManager = new NameManager(this);
        nameManager.loadNames();

        CustomNameCommand command = new CustomNameCommand(this);
        getCommand("customname").setExecutor(command);
        getCommand("customname").setTabCompleter(command);

        guiListener = new GUIListener(this);
        ChatListener chatListener = new ChatListener(this, guiListener);

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CustomNamePlaceholder(this).register();
            getLogger().info("PlaceholderAPI gefunden!");
        }

        getLogger().info("CustomName aktiviert!");
    }

    @Override
    public void onDisable() {
        if (nameManager != null) {
            nameManager.saveNames();
        }
        getLogger().info("CustomName deaktiviert!");
    }

    private void setupNamesFile() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        namesFile = new File(getDataFolder(), "names.yml");
        if (!namesFile.exists()) {
            try {
                namesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        namesConfig = YamlConfiguration.loadConfiguration(namesFile);
    }

    public void saveNamesConfig() {
        try {
            namesConfig.save(namesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CustomNamePlugin getInstance() { return instance; }
    public NameManager getNameManager() { return nameManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public MiniMessage getMiniMessage() { return miniMessage; }
    public FileConfiguration getNamesConfig() { return namesConfig; }
    public GUIListener getGuiListener() { return guiListener; }
}