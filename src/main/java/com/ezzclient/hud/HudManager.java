package com.ezzclient.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.LinkedHashMap;
import java.util.Map;

public class HudManager {
    public interface HudRender { void draw(DrawContext ctx, int x, int y, HudElement e); }

    public static final Map<String, HudElement> elements = new LinkedHashMap<>();
    public static boolean editing = false;
    public static HudElement dragging = null;
    public static int dragDx, dragDy;

    public static HudElement element(String id, int defX, int defY) {
        return elements.computeIfAbsent(id, k -> new HudElement(k, defX, defY));
    }

    public static void hud(String id, DrawContext ctx, int defX, int defY, HudRender r) {
        HudElement e = element(id, defX, defY);
        int before = 0;
        r.draw(ctx, e.x, e.y, e);
        if (e.w <= 0) e.w = 60;
        if (e.h <= 0) e.h = 14;
        if (editing) {
            ctx.drawBorder(e.x - 3, e.y - 3, e.w + 6, e.h + 6, 0x8800E5A8);
        }
    }

    public static HudElement findAt(int mx, int my) {
        for (HudElement e : elements.values()) {
            if (mx >= e.x - 3 && mx <= e.x + e.w + 3 && my >= e.y - 3 && my <= e.y + e.h + 3) return e;
        }
        return null;
    }
}
