package mrzitrrone.placeholder;

import mrzitrrone.CustomNamePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomNamePlaceholder extends PlaceholderExpansion {

    private final CustomNamePlugin plugin;

    public CustomNamePlaceholder(CustomNamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customname";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MrZitrrone";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        if (identifier.equals("name")) {
            return plugin.getNameManager().getDisplayName(player);
        }

        if (identifier.equals("raw")) {
            String customName = plugin.getNameManager().getCustomName(player.getUniqueId());
            return customName != null ? customName : player.getName();
        }

        if (identifier.equals("isnick")) {
            return plugin.getNameManager().isNicked(player.getUniqueId()) ? "true" : "false";
        }

        return null;
    }
}