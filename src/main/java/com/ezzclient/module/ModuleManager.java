package com.ezzclient.module;

import com.ezzclient.EzzClient;
import com.ezzclient.config.ConfigManager;
import com.ezzclient.modules.AllModules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();

    public static void register(Module m) { MODULES.add(m); }

    public static void init() { AllModules.registerAll(); }

    public static Module get(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public static List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.category == c) out.add(m);
        return out;
    }

    public static void onTick(MinecraftClient mc) {
        for (Module m : MODULES) if (m.enabled) { try { m.onTick(mc); } catch (Exception ignored) {} }
    }

    public static void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter tick) {
        for (Module m : MODULES) if (m.enabled) { try { m.onHud(mc, ctx, tick); } catch (Exception ignored) {} }
        HudToasts.render(mc, ctx);
    }

    public static int enabledCount() {
        int n = 0; for (Module m : MODULES) if (m.enabled) n++;
        return n;
    }
}
