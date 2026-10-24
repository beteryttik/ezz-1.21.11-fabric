package com.ezzclient.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;

public class ColorSetting extends Setting {
    public int value;
    public ColorSetting(String name, int argb) { super(name); value = argb; }
    public static final int[] PALETTE = {
        0xFFFFFFFF, 0xFF00E5A8, 0xFF7C4DFF, 0xFFFF5555, 0xFF4FC3F7,
        0xFFFFB74D, 0xFFFF66C4, 0xFF9E9E9E, 0xFF000000
    };
    public void cycle() {
        for (int i = 0; i < PALETTE.length; i++) {
            if (PALETTE[i] == value) { value = PALETTE[(i + 1) % PALETTE.length]; return; }
        }
        value = PALETTE[0];
    }
    public JsonElement save() { return com.google.gson.JsonParser.parseString(String.valueOf(value)); }
    public void load(JsonObject p) { if (p.has(name)) value = p.get(name).getAsInt(); }
}
