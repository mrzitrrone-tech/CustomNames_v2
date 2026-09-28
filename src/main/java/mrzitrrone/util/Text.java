package mrzitrrone.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Zentrale MiniMessage Helfer-Klasse.
 *
 * Unterstützt komplettes MiniMessage (inkl. gradient, shadow, hover, ...)
 * und zusätzlich alte "&"-Farbcodes für Rückwärtskompatibilität.
 */
public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** <shadow:...> gibt es erst ab Adventure 4.18 / Minecraft 1.21.4 */
    private static final boolean SHADOW_SUPPORTED = classExists("net.kyori.adventure.text.format.ShadowColor");

    private static final java.util.regex.Pattern SHADOW_TAG =
            java.util.regex.Pattern.compile("(?i)</?shadow(:[^>]*)?>");

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private Text() {
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isShadowSupported() {
        return SHADOW_SUPPORTED;
    }

    public static MiniMessage mm() {
        return MM;
    }

    /**
     * Wandelt einen Text in eine Component um.
     * - Enthält der Text "&lt;" wird er als MiniMessage behandelt
     * - Ansonsten werden "&amp;"/"§" Farbcodes nach MiniMessage übersetzt
     */
    public static Component parse(String input, TagResolver... resolvers) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        return MM.deserialize(toMiniMessage(input), resolvers);
    }

    /**
     * Wie {@link #parse(String, TagResolver...)}, jedoch ohne das
     * standardmäßige Kursiv von Item-Namen / Lore.
     */
    public static Component parseItem(String input, TagResolver... resolvers) {
        return parse(input, resolvers).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> parseItemLore(List<String> lines, TagResolver... resolvers) {
        List<Component> lore = new ArrayList<>();
        if (lines == null) {
            return lore;
        }
        for (String line : lines) {
            lore.add(parseItem(line, resolvers));
        }
        return lore;
    }

    /**
     * Normalisiert einen Eingabetext zu einem gültigen MiniMessage-String.
     */
    public static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (!SHADOW_SUPPORTED && input.indexOf('<') >= 0) {
            // Älterer Server: <shadow> würde als Text angezeigt werden -> entfernen
            input = SHADOW_TAG.matcher(input).replaceAll("");
        }
        if (input.indexOf('<') >= 0) {
            // Bereits MiniMessage (evtl. gemischt mit §-Codes vom Client)
            if (input.indexOf('\u00A7') >= 0) {
                return MM.serialize(SECTION.deserialize(input));
            }
            return input;
        }
        if (input.indexOf('&') >= 0 || input.indexOf('\u00A7') >= 0) {
            String normalized = input.replace('\u00A7', '&');
            return MM.serialize(LEGACY.deserialize(normalized));
        }
        return input;
    }

    /** Prüft, ob ein MiniMessage-String gültig ist. */
    public static boolean isValid(String input) {
        if (input == null) {
            return false;
        }
        try {
            MM.deserialize(toMiniMessage(input));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Text ohne jegliche Formatierung. */
    public static String plain(String input) {
        try {
            return PlainTextComponentSerializer.plainText().serialize(parse(input));
        } catch (Exception e) {
            return input == null ? "" : input;
        }
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /** Legacy (§) Darstellung – z.B. für PlaceholderAPI. */
    public static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /** Erstellt einen einfachen Platzhalter-Resolver. */
    public static TagResolver placeholder(String name, String value) {
        return TagResolver.resolver(name, Tag.inserting(Component.text(value == null ? "" : value)));
    }

    public static TagResolver placeholder(String name, Component value) {
        return TagResolver.resolver(name, Tag.inserting(value == null ? Component.empty() : value));
    }
}
