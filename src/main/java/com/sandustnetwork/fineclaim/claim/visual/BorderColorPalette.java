package com.sandustnetwork.fineclaim.claim.visual;

import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BorderColorPalette {

    private static final List<Color> DEFAULT_COLORS = List.of(
            Color.fromRGB(0x00, 0xB4, 0xFC),
            Color.fromRGB(0x6E, 0xE7, 0xFF),
            Color.fromRGB(0xA7, 0x8B, 0xFA),
            Color.fromRGB(0xF4, 0x72, 0xB6),
            Color.fromRGB(0x34, 0xD3, 0x99)
    );

    private final List<Color> colors;

    public BorderColorPalette(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            this.colors = DEFAULT_COLORS;
        } else {
            this.colors = List.copyOf(colors);
        }
    }

    public static BorderColorPalette fromConfig(FileConfiguration config) {
        List<String> rawColors = config.getStringList("BorderColors");
        if (rawColors.isEmpty()) {
            return new BorderColorPalette(DEFAULT_COLORS);
        }

        List<Color> parsed = new ArrayList<>(rawColors.size());
        for (String rawColor : rawColors) {
            parsed.add(parseColor(rawColor));
        }
        return new BorderColorPalette(parsed);
    }

    public Color colorForEdge(int edgeIndex, double progress) {
        int baseIndex = Math.floorMod(edgeIndex, colors.size());
        int nextIndex = (baseIndex + 1) % colors.size();
        return blend(colors.get(baseIndex), colors.get(nextIndex), progress);
    }

    public Color colorForCorner(int cornerIndex) {
        return colors.get(Math.floorMod(cornerIndex, colors.size()));
    }

    public List<Color> colors() {
        return colors;
    }

    private static Color parseColor(String rawColor) {
        String normalized = rawColor.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6) {
            throw new IllegalArgumentException("Invalid BorderColors entry: " + rawColor);
        }
        int rgb = Integer.parseUnsignedInt(normalized, 16);
        return Color.fromRGB(
                (rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF
        );
    }

    private static Color blend(Color from, Color to, double progress) {
        double clamped = Math.clamp(progress, 0.0, 1.0);
        int red = (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * clamped);
        int green = (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
        int blue = (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
        return Color.fromRGB(red, green, blue);
    }
}
