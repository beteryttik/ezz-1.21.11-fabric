package com.ezzclient.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }
    public abstract JsonElement save();
    public abstract void load(JsonObject parent);
}
