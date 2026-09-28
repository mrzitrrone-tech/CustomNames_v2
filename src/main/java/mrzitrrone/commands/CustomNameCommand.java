package mrzitrrone.commands;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.gui.CustomNameGUI;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.manager.NameManager;
import mrzitrrone.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomNameCommand implements CommandExecutor, TabCompleter {

    private final CustomNamePlugin plugin;

    public CustomNameCommand(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    private NameManager nameManager() {
        return plugin.getNameManager();
    }

    private ConfigManager config() {
        return plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("customname.admin")) {
                sender.sendMessage(config().msg("no-permission"));
                return true;
            }
            config().reloadConfigs();
            plugin.getPrefixManager().load();
            Bukkit.getOnlinePlayers().forEach(p -> nameManager().updatePlayerDisplayName(p));
            sender.sendMessage(config().msg("reload-success"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(config().msg("players-only"));
            return true;
        }

        if (!player.hasPermission("customname.use") && !player.hasPermission("customname.admin")) {
            player.sendMessage(config().msg("no-permission"));
            return true;
        }

        if (args.length == 0) {
            new CustomNameGUI(plugin, player).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            nameManager().removeCustomName(player);
            player.sendMessage(config().msg("name-reset"));
            return true;
        }

        if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            new CustomNameGUI(plugin, player).open();
            return true;
        }

        String fullMessage = String.join(" ", args);

        if (!Text.isValid(fullMessage)) {
            player.sendMessage(config().msg("invalid-format"));
            return true;
        }

        boolean isNick = isNickname(player.getName(), fullMessage);
        if (isNick && !player.hasPermission("customname.nick") && !player.hasPermission("customname.admin")) {
            player.sendMessage(config().msg("no-permission-nick"));
            return true;
        }

        String plainText = Text.plain(fullMessage);
        int max = plugin.getConfig().getInt("name.max-length", 32);
        if (plainText.length() > max) {
            player.sendMessage(config().msg("name-too-long", Text.placeholder("max", String.valueOf(max))));
            return true;
        }

        nameManager().setCustomName(player, Text.toMiniMessage(fullMessage), isNick);
        player.sendMessage(config().msg("name-changed", Text.placeholder("name", Text.parse(fullMessage))));
        return true;
    }

    private boolean isNickname(String realName, String miniMessage) {
        String plainText = Text.plain(miniMessage).toLowerCase(Locale.ROOT).trim();
        return !plainText.equals(realName.toLowerCase(Locale.ROOT));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reset");
            completions.add("gui");
            if (sender.hasPermission("customname.admin")) {
                completions.add("reload");
            }
            if (sender instanceof Player player) {
                completions.add("<gradient:#7688FF:#9584FF>" + player.getName() + "</gradient>");
                completions.add("<rainbow>" + player.getName() + "</rainbow>");
            }

            String start = args[0].toLowerCase(Locale.ROOT);
            List<String> filtered = new ArrayList<>();
            for (String s : completions) {
                if (s.toLowerCase(Locale.ROOT).startsWith(start)) {
                    filtered.add(s);
                }
            }
            return filtered;
        }

        return completions;
    }
}
