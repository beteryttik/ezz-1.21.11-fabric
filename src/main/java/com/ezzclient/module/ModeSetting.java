package com.ezzclient.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ModeSetting extends Setting {
    public final String[] modes; public int index;
    public ModeSetting(String name, String... modes) { super(name); this.modes = modes; }
    public String get() { return modes[index]; }
    public int cycle(int dir) { index = (index + dir + modes.length) % modes.length; return index; }
    public JsonElement save() { return com.google.gson.JsonParser.parseString(get()); }
    public void load(JsonObject p) {
        if (p.has(name)) {
            String v = p.get(name).getAsString();
            for (int i = 0; i < modes.length; i++) if (modes[i].equalsIgnoreCase(v)) index = i;
        }
    }
}
