package mrzitrrone.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * InventoryHolder für alle GUIs des Plugins.
 * Dadurch ist keine (fehleranfällige) Titel-Prüfung mehr nötig und
 * MiniMessage-Titel funktionieren problemlos.
 */
public class CustomNameHolder implements InventoryHolder {

    public enum MenuType {
        MAIN,
        COLORS
    }

    private final MenuType menuType;
    private final Map<Integer, MenuAction> actions = new HashMap<>();
    private Inventory inventory;

    public CustomNameHolder(MenuType menuType) {
        this.menuType = menuType;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setAction(int slot, MenuAction action) {
        actions.put(slot, action);
    }

    public MenuAction getAction(int slot) {
        return actions.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
