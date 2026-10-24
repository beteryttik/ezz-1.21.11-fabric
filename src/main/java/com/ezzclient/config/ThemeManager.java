package com.ezzclient.config;

import com.ezzclient.cosmetics.CosmeticManager;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ThemeManager {
    public static class Theme {
        public final String name;
        public final int accent, panel, card, text;
        public Theme(String name, int accent, int panel, int card, int text) {
            this.name = name; this.accent = accent; this.panel = panel; this.card = card; this.text = text;
        }
    }

    public static final Map<String, Theme> THEMES = new LinkedHashMap<>();
    static {
        THEMES.put("Midnight Bloom", new Theme("Midnight Bloom", 0xFF7C4DFF, 0xE611131C, 0xFF1A1E2E, 0xFFE8E8F0));
        THEMES.put("Emerald",        new Theme("Emerald",        0xFF00E5A8, 0xE60D1414, 0xFF12201A, 0xFFE8FFF5));
        THEMES.put("Crimson",        new Theme("Crimson",        0xFFFF4455, 0xE6160D12, 0xFF241218, 0xFFFFECEF));
        THEMES.put("Ocean",          new Theme("Ocean",          0xFF4FC3F7, 0xE60C1218, 0xFF12222E, 0xFFE6F6FF));
        THEMES.put("Sunset",         new Theme("Sunset",         0xFFFFB74D, 0xE6181008, 0xFF2A1F12, 0xFFFFF4E6));
        THEMES.put("Mono",           new Theme("Mono",           0xFFAAAAAA, 0xE6101010, 0xFF1A1A1A, 0xFFF2F2F2));
    }

    public static String current = "Midnight Bloom";

    public static Theme theme() { return THEMES.getOrDefault(current, THEMES.get("Midnight Bloom")); }
    public static void cycle(int dir) {
        java.util.List<String> keys = new java.util.ArrayList<>(THEMES.keySet());
        int i = keys.indexOf(current);
        current = keys.get((i + dir + keys.size()) % keys.size());
        ConfigManager.save();
    }

    public static void saveJson(JsonObject o) { o.addProperty("theme", current); }
    public static void loadJson(JsonObject o) { if (o.has("theme")) current = o.get("theme").getAsString(); }
}
