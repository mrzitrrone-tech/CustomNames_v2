package mrzitrrone.commands;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.gui.CustomNameGUI;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.manager.NameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomNameCommand implements CommandExecutor, TabCompleter {

    private final CustomNamePlugin plugin;
    private final NameManager nameManager;
    private final ConfigManager configManager;

    public CustomNameCommand(CustomNamePlugin plugin) {
        this.plugin = plugin;
        this.nameManager = plugin.getNameManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        if (!player.hasPermission("customname.use") && !player.hasPermission("customname.admin")) {
            player.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            new CustomNameGUI(plugin, player).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            nameManager.removeCustomName(player);
            player.sendMessage(configManager.getMessage("name-reset"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && player.hasPermission("customname.admin")) {
            configManager.reloadConfigs();
            player.sendMessage(configManager.getMessage("reload-success"));
            return true;
        }

        String fullMessage = String.join(" ", args);
        boolean isNick = isNickname(player.getName(), fullMessage);

        if (isNick && !player.hasPermission("customname.nick") && !player.hasPermission("customname.admin")) {
            player.sendMessage(configManager.getMessage("no-permission-nick"));
            return true;
        }

        if (!validateMiniMessage(fullMessage)) {
            player.sendMessage(configManager.getMessage("invalid-format"));
            return true;
        }

        String plainText = nameManager.stripMiniMessage(fullMessage);
        if (plainText.length() > 32) {
            player.sendMessage(configManager.getMessage("name-too-long"));
            return true;
        }

        nameManager.setCustomName(player, fullMessage, isNick);
        player.sendMessage(configManager.getMessage("name-changed"));
        return true;
    }

    private boolean isNickname(String realName, String miniMessage) {
        String plainText = nameManager.stripMiniMessage(miniMessage).toLowerCase().trim();
        return !plainText.equals(realName.toLowerCase());
    }

    private boolean validateMiniMessage(String text) {
        try {
            plugin.getMiniMessage().deserialize(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reset");
            if (sender.hasPermission("customname.admin")) {
                completions.add("reload");
            }
            if (sender instanceof Player player) {
                completions.add("<gradient:#FF0000:#FFFFFF>" + player.getName() + "</gradient>");
                completions.add("<rainbow>" + player.getName() + "</rainbow>");
            }
        }

        return completions;
    }
}