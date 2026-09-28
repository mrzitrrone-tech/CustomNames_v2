package mrzitrrone.gui;

/**
 * Eine Aktion, die an einem GUI-Slot hängt.
 */
public record MenuAction(Type type, String value) {

    public enum Type {
        /** Öffnet das Farb-Auswahl-Menü */
        OPEN_COLORS,
        /** Startet die MiniMessage Chat-Eingabe für den Namen */
        INPUT_NAME,
        /** Startet die MiniMessage Chat-Eingabe für das Prefix */
        INPUT_PREFIX,
        /** Setzt den Namen zurück */
        RESET_NAME,
        /** Entfernt das Prefix */
        RESET_PREFIX,
        /** Schließt das GUI */
        CLOSE,
        /** Zurück ins Hauptmenü */
        BACK,
        /** Wählt eine Farbe (value = MiniMessage Format) */
        SELECT_COLOR,
        /** Keine Aktion (Deko / Vorschau) */
        NONE
    }

    public static MenuAction of(Type type) {
        return new MenuAction(type, null);
    }
}
