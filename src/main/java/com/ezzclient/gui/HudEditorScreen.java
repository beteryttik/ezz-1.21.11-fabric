package com.ezzclient.gui;

import com.ezzclient.config.ConfigManager;
import com.ezzclient.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.ezzclient.hud.HudManager.*;

/** Редактор раскладки HUD: тяни элементы мышкой, ESC — сохранить и выйти. */
public class HudEditorScreen extends Screen {
    public HudEditorScreen() { super(Text.literal("hud editor")); }
    @Override public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        for (HudElement e : elements.values()) {
            ctx.fill(e.x, e.y, e.x + e.w, e.y + e.h, 0x5500E5A8);
            ctx.drawBorder(e.x - 2, e.y - 2, e.w + 4, e.h + 4, 0xFF00E5A8);
            ctx.drawTextWithShadow(textRenderer, e.id, e.x, e.y - 12, 0xFFB8BCD0);
        }
        ctx.drawTextWithShadow(textRenderer, "Drag elements. ESC to save & exit.", 6, height - 16, 0xFFFFFFFF);
        if (dragging == null) {
            HudElement hit = findAt(mx, my);
            if (hit != null) ctx.drawBorder(hit.x - 4, hit.y - 4, hit.w + 8, hit.h + 8, 0xFFFF6688);
        }
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        HudElement e = findAt((int) mx, (int) my);
        if (e != null) { dragging = e; dragDx = (int) mx - e.x; dragDy = (int) my - e.y; }
        return true;
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) { dragging.x = (int) mx - dragDx; dragging.y = (int) my - dragDy; }
        return true;
    }

    @Override public boolean mouseReleased(double mx, double my, int button) { dragging = null; return true; }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) {
            editing = false; ConfigManager.save(); close(); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
