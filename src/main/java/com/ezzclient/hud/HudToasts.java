package com.ezzclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HudToasts {
    private static final List<String[]> active = new ArrayList<>();

    public static void push(String text) {
        active.add(new String[]{text, String.valueOf(System.currentTimeMillis())});
        if (active.size() > 5) active.remove(0);
    }

    public static void render(MinecraftClient mc, DrawContext ctx) {
        Iterator<String[]> it = active.iterator();
        int y = 4;
        while (it.hasNext()) {
            String[] t = it.next();
            long age = System.currentTimeMillis() - Long.parseLong(t[1]);
            if (age > 3500) { it.remove(); continue; }
            int w = mc.textRenderer.getWidth(t[0]) + 12;
            ctx.fill(4, y, 4 + w, y + 16, 0xB0101018);
            ctx.fill(4, y + 15, 4 + w, y + 16, 0xFF00E5A8);
            ctx.drawTextWithShadow(mc.textRenderer, t[0], 10, y + 4, Formatting.WHITE.getColorValue());
            y += 18;
        }
    }
}
