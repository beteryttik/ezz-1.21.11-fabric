package com.ezzclient.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BooleanSetting extends Setting {
    public boolean value;
    public BooleanSetting(String name, boolean def) { super(name); value = def; }
    public void set(boolean v) { value = v; }
    public JsonElement save() { return com.google.gson.JsonParser.parseString(String.valueOf(value)); }
    public void load(JsonObject p) { if (p.has(name)) value = p.get(name).getAsBoolean(); }
}
