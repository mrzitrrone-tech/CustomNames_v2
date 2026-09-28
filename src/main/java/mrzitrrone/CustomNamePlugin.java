package mrzitrrone;

import mrzitrrone.commands.CustomNameCommand;
import mrzitrrone.commands.PrefixCommand;
import mrzitrrone.listeners.ChatListener;
import mrzitrrone.listeners.GUIListener;
import mrzitrrone.listeners.JoinListener;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.manager.NameManager;
import mrzitrrone.manager.PrefixManager;
import mrzitrrone.placeholder.CustomNamePlaceholder;
import mrzitrrone.util.SchedulerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomNamePlugin extends JavaPlugin {

    private static CustomNamePlugin instance;

    private NameManager nameManager;
    private PrefixManager prefixManager;
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

        prefixManager = new PrefixManager(this);
        prefixManager.load();

        registerCommand("customname", new CustomNameCommand(this));
        registerCommand("prefix", new PrefixCommand(this));

        guiListener = new GUIListener(this);
        ChatListener chatListener = new ChatListener(this, guiListener);

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(guiListener, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CustomNamePlaceholder(this).register();
            getLogger().info("PlaceholderAPI gefunden!");
        }

        getLogger().info("CustomName aktiviert! (Scheduler: "
                + (SchedulerUtil.isFolia() ? "Folia" : "Paper/Spigot") + ")");
    }

    @Override
    public void onDisable() {
        if (nameManager != null) {
            nameManager.saveNamesSync();
        }
        if (prefixManager != null) {
            prefixManager.saveSync();
        }
        getLogger().info("CustomName deaktiviert!");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Befehl '" + name + "' fehlt in der plugin.yml!");
            return;
        }
        command.setExecutor((org.bukkit.command.CommandExecutor) executor);
        if (executor instanceof org.bukkit.command.TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    private void setupNamesFile() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        namesFile = new File(getDataFolder(), "names.yml");
        if (!namesFile.exists()) {
            try {
                namesFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Konnte names.yml nicht erstellen: " + e.getMessage());
            }
        }
        namesConfig = YamlConfiguration.loadConfiguration(namesFile);
    }

    public void saveNamesConfig() {
        try {
            namesConfig.save(namesFile);
        } catch (IOException e) {
            getLogger().warning("Konnte names.yml nicht speichern: " + e.getMessage());
        }
    }

    public static CustomNamePlugin getInstance() { return instance; }
    public NameManager getNameManager() { return nameManager; }
    public PrefixManager getPrefixManager() { return prefixManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public MiniMessage getMiniMessage() { return miniMessage; }
    public FileConfiguration getNamesConfig() { return namesConfig; }
    public GUIListener getGuiListener() { return guiListener; }
}
