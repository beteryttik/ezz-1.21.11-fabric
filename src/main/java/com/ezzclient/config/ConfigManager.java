package com.ezzclient.config;

import com.ezzclient.cosmetics.CosmeticManager;
import com.ezzclient.hud.HudManager;
import com.ezzclient.module.Module;
import com.ezzclient.module.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static String currentProfile = "default";

    private static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("ezzclient");
    }

    public static List<String> profiles() {
        List<String> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir(), "*.json")) {
            for (Path p : ds) {
                String n = p.getFileName().toString();
                out.add(n.substring(0, n.length() - 5));
            }
        } catch (IOException ignored) {}
        if (!out.contains("default")) out.add(0, "default");
        return out;
    }

    public static void load() {
        try { Files.createDirectories(dir()); } catch (IOException ignored) {}
        loadProfile("default");
    }

    public static void loadProfile(String name) {
        currentProfile = name;
        Path f = dir().resolve(name + ".json");
        try {
            if (Files.exists(f)) {
                JsonObject o = GSON.fromJson(Files.readString(f), JsonObject.class);
                if (o.has("mods")) {
                    JsonObject mods = o.getAsJsonObject("mods");
                    for (Module m : ModuleManager.MODULES) m.loadState(mods);
                }
                if (o.has("hud")) {
                    JsonObject hud = o.getAsJsonObject("hud");
                    for (String key : hud.keySet()) {
                        JsonObject el = hud.getAsJsonObject(key);
                        HudManager.element(key, 0, 0).x = el.get("x").getAsInt();
                        HudManager.element(key, 0, 0).y = el.get("y").getAsInt();
                    }
                }
                if (o.has("cosmetics")) CosmeticManager.loadJson(o.getAsJsonObject("cosmetics"));
                if (o.has("theme")) ThemeManager.loadJson(o);
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.createDirectories(dir());
            JsonObject o = new JsonObject();
            JsonObject mods = new JsonObject();
            for (Module m : ModuleManager.MODULES) m.saveState(mods);
            o.add("mods", mods);
            JsonObject hud = new JsonObject();
            for (HudManager.HudElement e : HudManager.elements.values()) {
                JsonObject el = new JsonObject();
                el.addProperty("x", e.x); el.addProperty("y", e.y);
                hud.add(e.id, el);
            }
            o.add("hud", hud);
            o.add("cosmetics", CosmeticManager.saveJson());
            ThemeManager.saveJson(o);
            Files.writeString(dir().resolve(currentProfile + ".json"), GSON.toJson(o));
        } catch (Exception ignored) {}
    }

    public static void createProfile(String name) {
        Path f = dir().resolve(name + ".json");
        if (Files.exists(f)) return;
        String prev = currentProfile;
        currentProfile = name;
        save();
        currentProfile = prev;
    }

    public static void deleteProfile(String name) {
        if (name.equals("default")) return;
        try { Files.deleteIfExists(dir().resolve(name + ".json")); } catch (IOException ignored) {}
    }
}
