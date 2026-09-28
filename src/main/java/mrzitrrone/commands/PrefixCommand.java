package mrzitrrone.commands;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.gui.CustomNameGUI;
import mrzitrrone.manager.ConfigManager;
import mrzitrrone.manager.PrefixManager;
import mrzitrrone.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.UUID;

/**
 * /prefix &lt;MiniMessage&gt;
 *
 * Beispiel:
 * /prefix &lt;b&gt;&lt;gradient:#7688FF:#9584FF&gt;&lt;shadow:#183931:1&gt;Gooner&lt;/shadow&gt;&lt;/gradient&gt;&lt;/b&gt;
 */
public class PrefixCommand implements CommandExecutor, TabCompleter {

    private static final String EXAMPLE =
            "<b><gradient:#7688FF:#9584FF><shadow:#183931:1>Gooner</shadow></gradient></b>";

    private final CustomNamePlugin plugin;

    public PrefixCommand(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    private PrefixManager prefixManager() {
        return plugin.getPrefixManager();
    }

    private ConfigManager config() {
        return plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!plugin.getConfig().getBoolean("prefix.enabled", true)) {
            sender.sendMessage(config().msg("prefix-disabled"));
            return true;
        }

        // ===== /prefix reload =====
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("customname.admin")) {
                sender.sendMessage(config().msg("no-permission"));
                return true;
            }
            config().reloadConfigs();
            prefixManager().load();
            Bukkit.getOnlinePlayers().forEach(p -> plugin.getNameManager().updatePlayerDisplayName(p));
            sender.sendMessage(config().msg("reload-success"));
            return true;
        }

        // ===== /prefix set <spieler> <prefix...> =====
        if (args.length >= 3 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("customname.prefix.others") && !sender.hasPermission("customname.admin")) {
                sender.sendMessage(config().msg("no-permission"));
                return true;
            }
            OfflinePlayer target = resolve(args[1]);
            if (target == null) {
                sender.sendMessage(config().msg("player-not-found", Text.placeholder("player", args[1])));
                return true;
            }
            String raw = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            if (!applyPrefix(sender, target.getUniqueId(), raw)) {
                return true;
            }
            sender.sendMessage(config().msg("prefix-changed-other",
                    Text.placeholder("player", target.getName() == null ? args[1] : target.getName()),
                    Text.placeholder("prefix", Text.parse(raw))));
            return true;
        }

        // ===== /prefix reset [spieler] =====
        if (args.length >= 1 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("remove"))) {
            if (args.length >= 2) {
                if (!sender.hasPermission("customname.prefix.others") && !sender.hasPermission("customname.admin")) {
                    sender.sendMessage(config().msg("no-permission"));
                    return true;
                }
                OfflinePlayer target = resolve(args[1]);
                if (target == null) {
                    sender.sendMessage(config().msg("player-not-found", Text.placeholder("player", args[1])));
                    return true;
                }
                prefixManager().removePrefix(target.getUniqueId());
                sender.sendMessage(config().msg("prefix-reset-other",
                        Text.placeholder("player", target.getName() == null ? args[1] : target.getName())));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config().msg("players-only"));
                return true;
            }
            prefixManager().removePrefix(player.getUniqueId());
            player.sendMessage(config().msg("prefix-reset"));
            return true;
        }

        // ===== Ab hier nur Spieler =====
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config().msg("players-only"));
            return true;
        }

        if (!player.hasPermission("customname.prefix.use") && !player.hasPermission("customname.admin")) {
            player.sendMessage(config().msg("no-permission-prefix"));
            return true;
        }

        // ===== /prefix (ohne Argumente) =====
        if (args.length == 0) {
            if (plugin.getConfig().getBoolean("prefix.open-gui-without-args", true)) {
                new CustomNameGUI(plugin, player).open();
            } else {
                player.sendMessage(config().msg("prefix-usage"));
                player.sendMessage(config().msgRaw("enter-prefix-example"));
            }
            return true;
        }

        // ===== /prefix gui =====
        if (args.length == 1 && (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu"))) {
            new CustomNameGUI(plugin, player).open();
            return true;
        }

        // ===== /prefix <MiniMessage> =====
        String raw = String.join(" ", args);
        if (!applyPrefix(player, player.getUniqueId(), raw)) {
            return true;
        }
        player.sendMessage(config().msg("prefix-changed", Text.placeholder("prefix", Text.parse(raw))));
        return true;
    }

    /** Validiert und setzt das Prefix. Gibt false zurück, wenn etwas ungültig war. */
    private boolean applyPrefix(CommandSender sender, UUID uuid, String raw) {
        if (raw == null || raw.isBlank()) {
            sender.sendMessage(config().msg("prefix-usage"));
            return false;
        }

        if (!Text.isValid(raw)) {
            sender.sendMessage(config().msg("invalid-format"));
            return false;
        }

        String plain = Text.plain(raw);
        int max = prefixManager().getMaxLength();
        if (plain.length() > max) {
            sender.sendMessage(config().msg("prefix-too-long", Text.placeholder("max", String.valueOf(max))));
            return false;
        }

        List<String> blocked = plugin.getConfig().getStringList("prefix.blocked-words");
        String lower = plain.toLowerCase(Locale.ROOT);
        for (String word : blocked) {
            if (!word.isEmpty() && lower.contains(word.toLowerCase(Locale.ROOT))) {
                sender.sendMessage(config().msg("prefix-blocked"));
                return false;
            }
        }

        prefixManager().setPrefix(uuid, Text.toMiniMessage(raw));
        return true;
    }

    private OfflinePlayer resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        return offline.hasPlayedBefore() ? offline : null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reset");
            completions.add("gui");
            completions.add(EXAMPLE);
            completions.add("<gradient:#FF0000:#FFAA00>VIP</gradient>");
            if (sender.hasPermission("customname.prefix.others") || sender.hasPermission("customname.admin")) {
                completions.add("set");
            }
            if (sender.hasPermission("customname.admin")) {
                completions.add("reload");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))
                && (sender.hasPermission("customname.prefix.others") || sender.hasPermission("customname.admin"))) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            return filter(completions, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.add(EXAMPLE);
        }

        return completions;
    }

    private List<String> filter(List<String> input, String start) {
        String lower = start.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String s : input) {
            if (s.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
