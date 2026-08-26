package mrzitrrone.gui;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.manager.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomNameGUI {

    private final CustomNamePlugin plugin;
    private final Player player;
    private final ConfigManager config;

    public CustomNameGUI(CustomNamePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfigManager();
    }

    public void open() {
        int rows = plugin.getConfig().getInt("gui.rows", 4);
        String title = config.colorize(plugin.getConfig().getString("gui.title", "&6&lCustomName Menü"));

        Inventory inv = Bukkit.createInventory(null, rows * 9, Component.text(title));

        ItemStack presetItem = createItem(
                Material.valueOf(plugin.getConfig().getString("gui.preset-item.material", "GRAY_DYE")),
                plugin.getConfig().getString("gui.preset-item.name"),
                plugin.getConfig().getStringList("gui.preset-item.lore")
        );
        inv.setItem(11, presetItem);

        ItemStack miniMessageItem = createItem(
                Material.valueOf(plugin.getConfig().getString("gui.minimessage-item.material", "WRITABLE_BOOK")),
                plugin.getConfig().getString("gui.minimessage-item.name"),
                plugin.getConfig().getStringList("gui.minimessage-item.lore")
        );
        inv.setItem(13, miniMessageItem);

        ItemStack resetItem = createItem(
                Material.valueOf(plugin.getConfig().getString("gui.reset-item.material", "BARRIER")),
                plugin.getConfig().getString("gui.reset-item.name"),
                plugin.getConfig().getStringList("gui.reset-item.lore")
        );
        inv.setItem(15, resetItem);

        player.openInventory(inv);
    }

    public void openPresetMenu() {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§7Farb-Presets"));

        List<String> presets = plugin.getConfig().getStringList("presets");

        for (int i = 0; i < Math.min(presets.size(), 15); i++) {
            String preset = presets.get(i);
            String colorCode = preset.substring(0, 2);
            String colorName = preset.substring(2);

            Material dyeMaterial = getDyeFromColorCode(colorCode);

            ItemStack item = new ItemStack(dyeMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(config.colorize(colorCode + colorName)));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Klicke um diese Farbe"));
            lore.add(Component.text("§7für deinen Namen zu nutzen"));
            meta.lore(lore);
            item.setItemMeta(meta);

            inv.setItem(i, item);
        }

        ItemStack backButton = createItem(
                Material.valueOf(plugin.getConfig().getString("gui.back-button.material", "RED_STAINED_GLASS_PANE")),
                plugin.getConfig().getString("gui.back-button.name"),
                new ArrayList<>()
        );
        inv.setItem(26, backButton);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (name != null) {
            meta.displayName(Component.text(config.colorize(name)));
        }

        if (lore != null && !lore.isEmpty()) {
            List<Component> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(Component.text(config.colorize(line)));
            }
            meta.lore(coloredLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    private Material getDyeFromColorCode(String colorCode) {
        switch (colorCode) {
            case "&c": case "&4": return Material.RED_DYE;
            case "&6": return Material.ORANGE_DYE;
            case "&e": return Material.YELLOW_DYE;
            case "&a": case "&2": return Material.LIME_DYE;
            case "&b": return Material.CYAN_DYE;
            case "&9": case "&1": return Material.BLUE_DYE;
            case "&d": return Material.PINK_DYE;
            case "&5": return Material.PURPLE_DYE;
            case "&f": return Material.WHITE_DYE;
            case "&7": return Material.LIGHT_GRAY_DYE;
            case "&8": return Material.GRAY_DYE;
            case "&0": return Material.BLACK_DYE;
            default: return Material.GRAY_DYE;
        }
    }
}