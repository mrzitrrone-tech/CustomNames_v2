package mrzitrrone.gui;

import mrzitrrone.CustomNamePlugin;
import mrzitrrone.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Baut die GUIs des Plugins komplett aus der config.yml.
 * Jeder Slot, jedes Item und jede Lore-Zeile ist konfigurierbar.
 */
public class CustomNameGUI {

    private final CustomNamePlugin plugin;
    private final Player player;

    public CustomNameGUI(CustomNamePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    // ==================================================================
    //  HAUPTMENÜ
    // ==================================================================
    public void open() {
        ConfigurationSection gui = section("gui");
        int rows = clampRows(gui == null ? 6 : gui.getInt("rows", 6));
        Component title = Text.parse(gui == null
                ? "<gray>CustomName"
                : gui.getString("title", "<gray>CustomName"), resolvers());

        CustomNameHolder holder = new CustomNameHolder(CustomNameHolder.MenuType.MAIN);
        Inventory inv = Bukkit.createInventory(holder, rows * 9, title);
        holder.bind(inv);

        Set<Integer> reserved = new HashSet<>();
        fill(inv, holder, gui == null ? null : gui.getConfigurationSection("filler"));

        if (gui != null) {
            ConfigurationSection items = gui.getConfigurationSection("items");
            if (items != null) {
                place(inv, holder, reserved, items.getConfigurationSection("preview"), MenuAction.of(MenuAction.Type.NONE), 4);
                place(inv, holder, reserved, items.getConfigurationSection("presets"), MenuAction.of(MenuAction.Type.OPEN_COLORS), 20);
                place(inv, holder, reserved, items.getConfigurationSection("minimessage"), MenuAction.of(MenuAction.Type.INPUT_NAME), 22);
                place(inv, holder, reserved, items.getConfigurationSection("prefix"), MenuAction.of(MenuAction.Type.INPUT_PREFIX), 24);
                place(inv, holder, reserved, items.getConfigurationSection("reset"), MenuAction.of(MenuAction.Type.RESET_NAME), 38);
                place(inv, holder, reserved, items.getConfigurationSection("reset-prefix"), MenuAction.of(MenuAction.Type.RESET_PREFIX), 42);
                place(inv, holder, reserved, items.getConfigurationSection("close"), MenuAction.of(MenuAction.Type.CLOSE), 49);
            }
        }

        player.openInventory(inv);
    }

    // ==================================================================
    //  FARB-AUSWAHL (Selector)
    // ==================================================================
    public void openColorMenu() {
        ConfigurationSection menu = section("color-menu");
        int rows = clampRows(menu == null ? 6 : menu.getInt("rows", 6));
        Component title = Text.parse(menu == null
                ? "<gray>Farben"
                : menu.getString("title", "<gray>Farben"), resolvers());

        CustomNameHolder holder = new CustomNameHolder(CustomNameHolder.MenuType.COLORS);
        Inventory inv = Bukkit.createInventory(holder, rows * 9, title);
        holder.bind(inv);

        Set<Integer> reserved = new HashSet<>();
        fill(inv, holder, menu == null ? null : menu.getConfigurationSection("filler"));

        if (menu != null) {
            place(inv, holder, reserved, menu.getConfigurationSection("preview"), MenuAction.of(MenuAction.Type.NONE), 4);
            place(inv, holder, reserved, menu.getConfigurationSection("back-button"), MenuAction.of(MenuAction.Type.BACK), 48);
            place(inv, holder, reserved, menu.getConfigurationSection("close-button"), MenuAction.of(MenuAction.Type.CLOSE), 50);

            // Farben: beliebig viele, einfach in der config.yml ergänzen
            List<Map<?, ?>> colors = menu.getMapList("colors");
            int auto = 0;
            int[] layout = autoSlots(rows);

            for (Map<?, ?> raw : colors) {
                String name = string(raw.get("name"), "<white>Farbe");
                String format = string(raw.get("format"), "<white><player>");
                String materialName = string(raw.get("material"), "WHITE_DYE");
                String permission = string(raw.get("permission"), "");
                Object slotObj = raw.get("slot");

                if (!permission.isEmpty() && !player.hasPermission(permission)) {
                    continue;
                }

                int slot;
                if (slotObj instanceof Number number) {
                    slot = number.intValue();
                } else {
                    while (auto < layout.length && reserved.contains(layout[auto])) {
                        auto++;
                    }
                    if (auto >= layout.length) {
                        plugin.getLogger().warning("Nicht genug freie Slots im Farb-Menü – "
                                + "erhöhe 'color-menu.rows' oder setze feste Slots.");
                        break;
                    }
                    slot = layout[auto++];
                }

                if (slot < 0 || slot >= inv.getSize()) {
                    plugin.getLogger().warning("Farbe '" + Text.plain(name) + "' hat einen ungültigen Slot: " + slot);
                    continue;
                }

                String resolvedFormat = applyPlayer(format);
                Component preview;
                try {
                    preview = Text.parse(resolvedFormat);
                } catch (Exception e) {
                    plugin.getLogger().warning("Ungültiges MiniMessage Format bei Farbe '"
                            + Text.plain(name) + "': " + format);
                    continue;
                }
                TagResolver[] colorResolvers = new TagResolver[]{
                        Text.placeholder("player", player.getName()),
                        Text.placeholder("preview", preview)
                };

                ItemStack item = buildItem(
                        materialName,
                        name,
                        stringList(raw.get("lore")),
                        colorResolvers,
                        toBoolean(raw.get("glow"))
                );
                inv.setItem(slot, item);
                reserved.add(slot);
                holder.setAction(slot, new MenuAction(MenuAction.Type.SELECT_COLOR, resolvedFormat));
            }
        }

        player.openInventory(inv);
    }

    // ==================================================================
    //  Helfer
    // ==================================================================
    private ConfigurationSection section(String path) {
        return plugin.getConfig().getConfigurationSection(path);
    }

    private int clampRows(int rows) {
        return Math.max(1, Math.min(6, rows));
    }

    /** Platzhalter für Titel, Namen und Lore. */
    private TagResolver[] resolvers() {
        String rawName = plugin.getNameManager().getCustomName(player.getUniqueId());
        Component nameComponent;
        try {
            nameComponent = rawName == null ? Component.text(player.getName()) : Text.parse(rawName);
        } catch (Exception e) {
            nameComponent = Component.text(player.getName());
        }
        Component prefixComponent = plugin.getPrefixManager().getPrefixComponent(player.getUniqueId());

        return new TagResolver[]{
                Text.placeholder("player", player.getName()),
                Text.placeholder("name", nameComponent),
                Text.placeholder("prefix", prefixComponent),
                Text.placeholder("raw_name", rawName == null ? player.getName() : rawName),
                Text.placeholder("raw_prefix",
                        plugin.getPrefixManager().getRawPrefix(player.getUniqueId()) == null
                                ? "-" : plugin.getPrefixManager().getRawPrefix(player.getUniqueId())),
                Text.placeholder("preview", plugin.getNameManager().buildDisplayName(player))
        };
    }

    private void fill(Inventory inv, CustomNameHolder holder, ConfigurationSection filler) {
        if (filler == null || !filler.getBoolean("enabled", false)) {
            return;
        }
        ItemStack item = buildItem(
                filler.getString("material", "BLACK_STAINED_GLASS_PANE"),
                filler.getString("name", " "),
                filler.getStringList("lore"),
                resolvers(),
                false
        );
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, item.clone());
            holder.setAction(i, MenuAction.of(MenuAction.Type.NONE));
        }
    }

    private void place(Inventory inv, CustomNameHolder holder, Set<Integer> reserved,
                       ConfigurationSection sec, MenuAction action, int defaultSlot) {
        if (sec == null || !sec.getBoolean("enabled", true)) {
            return;
        }
        int slot = sec.getInt("slot", defaultSlot);
        if (slot < 0 || slot >= inv.getSize()) {
            plugin.getLogger().warning("Ungültiger Slot " + slot + " in '" + sec.getCurrentPath()
                    + "' (GUI hat " + inv.getSize() + " Slots) – Item wird übersprungen.");
            return;
        }

        ItemStack item = buildItem(
                sec.getString("material", "PAPER"),
                sec.getString("name", " "),
                sec.getStringList("lore"),
                resolvers(),
                sec.getBoolean("glow", false)
        );
        inv.setItem(slot, item);
        reserved.add(slot);
        holder.setAction(slot, action);
    }

    private ItemStack buildItem(String materialName, String name, List<String> lore,
                                TagResolver[] resolvers, boolean glow) {
        Material material = Material.matchMaterial(materialName == null ? "" : materialName.toUpperCase());
        if (material == null || material.isAir()) {
            plugin.getLogger().warning("Unbekanntes Material: " + materialName + " – nutze PAPER");
            material = Material.PAPER;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (name != null && !name.isEmpty()) {
            meta.displayName(Text.parseItem(name, resolvers));
        }

        // Lore kommt ausschließlich aus der Config – keine fest einprogrammierten
        // Beschreibungstexte mehr.
        if (lore != null && !lore.isEmpty()) {
            meta.lore(Text.parseItemLore(lore, resolvers));
        } else {
            meta.lore(null);
        }

        // Versteckt Attribute / zusätzliche Item-Beschreibungen (z.B. Trank- oder
        // Verzauberungs-Tooltips) damit nur Name + Config-Lore sichtbar sind.
        meta.addItemFlags(ItemFlag.values());

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        item.setItemMeta(meta);

        if (glow) {
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
        }

        return item;
    }

    /** Standard-Slots für automatisch platzierte Farben (Innenbereich). */
    private int[] autoSlots(int rows) {
        int size = rows * 9;
        int count = 0;
        int[] tmp = new int[size];
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col <= 7; col++) {
                int slot = row * 9 + col;
                if (slot < size) {
                    tmp[count++] = slot;
                }
            }
        }
        int[] result = new int[count];
        System.arraycopy(tmp, 0, result, 0, count);
        return result;
    }

    private String applyPlayer(String format) {
        if (format == null) {
            return "";
        }
        return format.replace("%player%", player.getName())
                .replace("<player>", player.getName());
    }

    private static String string(Object value, String def) {
        return value == null ? def : String.valueOf(value);
    }

    private static boolean toBoolean(Object value) {
        return value instanceof Boolean b && b;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
