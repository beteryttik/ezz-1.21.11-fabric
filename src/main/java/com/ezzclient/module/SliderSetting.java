package com.ezzclient.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SliderSetting extends Setting {
    public double value, min, max, step;
    public SliderSetting(String name, double def, double min, double max, double step) {
        super(name); value = def; this.min = min; this.max = max; this.step = step;
    }
    public void set(double v) { value = Math.max(min, Math.min(max, v)); }
    public String display() {
        if (step >= 1) return String.valueOf((int) value);
        return String.format("%.1f", value);
    }
    public JsonElement save() { return com.google.gson.JsonParser.parseString(String.valueOf(value)); }
    public void load(JsonObject p) { if (p.has(name)) set(p.get(name).getAsDouble()); }
}
