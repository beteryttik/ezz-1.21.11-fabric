package com.ezzclient.module;

import com.ezzclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name, description;
    public final Category category;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    public void toggle() {
        enabled = !enabled;
        try { if (enabled) onEnable(); else onDisable(); } catch (Exception ignored) {}
        ConfigManager.save();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter tick) {}

    protected BooleanSetting bool(String name, boolean def) {
        BooleanSetting s = new BooleanSetting(name, def); settings.add(s); return s;
    }
    protected SliderSetting slider(String name, double def, double min, double max, double step) {
        SliderSetting s = new SliderSetting(name, def, min, max, step); settings.add(s); return s;
    }
    protected ModeSetting mode(String name, String... modes) {
        ModeSetting s = new ModeSetting(name, modes); settings.add(s); return s;
    }
    protected ColorSetting color(String name, int argb) {
        ColorSetting s = new ColorSetting(name, argb); settings.add(s); return s;
    }

    public void saveState(com.google.gson.JsonObject mods) {
        JsonObject o = new JsonObject();
        o.addProperty("enabled", enabled);
        JsonObject set = new JsonObject();
        for (Setting s : settings) set.add(s.name, s.save());
        o.add("settings", set);
        mods.add(name, o);
    }
    public void loadState(com.google.gson.JsonObject mods) {
        if (!mods.has(name)) return;
        JsonObject o = mods.getAsJsonObject(name);
        boolean was = enabled;
        enabled = o.has("enabled") && o.get("enabled").getAsBoolean();
        if (o.has("settings") && o.get("settings").isJsonObject())
            for (Setting s : settings) s.load(o.getAsJsonObject("settings"));
        if (was != enabled) { try { if (enabled) onEnable(); else onDisable(); } catch (Exception ignored) {} }
    }
}
