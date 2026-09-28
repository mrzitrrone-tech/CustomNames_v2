package mrzitrrone.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Kleine Abstraktion über Bukkit- und Folia-Scheduler.
 *
 * Auf Folia gibt es keinen globalen Haupt-Thread mehr, deshalb werden
 * Aufgaben über den GlobalRegionScheduler bzw. den EntityScheduler des
 * Spielers ausgeführt. Auf normalem Paper/Spigot wird auf den klassischen
 * BukkitScheduler zurückgefallen.
 */
public final class SchedulerUtil {

    private static final boolean FOLIA = detectFolia();

    private SchedulerUtil() {
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    /** Führt eine Aufgabe global (nicht entity-gebunden) aus. */
    public static void runGlobal(Plugin plugin, Runnable runnable) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, runnable);
        } else if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /** Führt eine Aufgabe global mit Verzögerung (in Ticks) aus. */
    public static void runGlobalLater(Plugin plugin, Runnable runnable, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /** Führt eine Aufgabe im Kontext des Spielers aus (Folia-sicher). */
    public static void runForPlayer(Plugin plugin, Player player, Runnable runnable) {
        if (player == null) {
            return;
        }
        if (FOLIA) {
            player.getScheduler().run(plugin, task -> runnable.run(), null);
        } else if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /** Führt eine Aufgabe im Kontext des Spielers verzögert aus. */
    public static void runForPlayerLater(Plugin plugin, Player player, Runnable runnable, long delayTicks) {
        if (player == null) {
            return;
        }
        long delay = Math.max(1L, delayTicks);
        if (FOLIA) {
            player.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /** Führt eine Aufgabe asynchron aus (z.B. Datei speichern). */
    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }
}
