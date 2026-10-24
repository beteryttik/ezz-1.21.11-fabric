package com.ezzclient.gui;

import com.ezzclient.config.ConfigManager;
import com.ezzclient.config.LangManager;
import com.ezzclient.config.ThemeManager;
import com.ezzclient.cosmetics.Cosmetic;
import com.ezzclient.cosmetics.CosmeticManager;
import com.ezzclient.module.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Главное окно Ezz Client в стиле Phaze:
 * вкладки MODS / SETTINGS / COSMETICS / CONFIGS, карточки модулей,
 * поиск, фильтры категорий, конфиги слева, превью косметики справа.
 */
public class ClickGUIScreen extends Screen {
    private final int PW = 920, PH = 560;
    private int px, py;
    private String tab = "MODS";
    private String filter = "ALL";
    private String search = "";
    private boolean typing = false;
    private int scrollMods = 0, scrollCos = 0;
    private String cosmeticCategory = "ALL";
    private Cosmetic selected = null;
    private String newConfigName = null;

    private record Rect(int x, int y, int w, int h) {}

    private final List<Object[]> clicks = new ArrayList<>(); // [Rect, Runnable]
    private final List<Object[]> sliders = new ArrayList<>(); // [Rect, SliderSetting]
    private final List<Object[]> cosCycles = new ArrayList<>(); // [Rect, Cosmetic]

    public ClickGUIScreen() { super(Text.literal("ezz client")); }

    @Override public boolean shouldPause() { return false; }

    private int accent() { return ThemeManager.theme().accent; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        clicks.clear(); sliders.clear(); cosCycles.clear();
        renderBackground(ctx, mouseX, mouseY, delta);

        px = (width - PW) / 2; py = (height - PH) / 2;
        int panel = ThemeManager.theme().panel, card = ThemeManager.theme().card;

        ctx.fill(px, py, px + PW, py + PH, panel);
        ctx.fill(px, py, px + PW, py + 3, accent());
        ctx.drawBorder(px, py, PW, PH, 0x66000000);
        ctx.drawTextWithShadow(textRenderer, "ezz client", px + 8, py + 8, accent());

        // вкладки
        String[] tabs = {"MODS", "SETTINGS", "COSMETICS", "CONFIGS"};
        int tx = px + 250;
        for (String t : tabs) {
            int w = textRenderer.getWidth(t) + 24;
            boolean on = t.equals(tab);
            ctx.fill(tx, py + 24, tx + w, py + 46, on ? 0xFF232838 : 0xFF171B26);
            if (on) ctx.fill(tx, py + 44, tx + w, py + 46, accent());
            ctx.drawTextWithShadow(textRenderer, t, tx + 12, py + 32, on ? 0xFFFFFFFF : 0xFF9A9EB0);
            final String tabName = t;
            clickable(new Rect(tx, py + 24, w, 22), () -> { tab = tabName; scrollMods = 0; scrollCos = 0; });
            tx += w + 8;
        }

        switch (tab) {
            case "MODS" -> renderMods(ctx, mouseX, mouseY, card);
            case "SETTINGS" -> renderSettings(ctx, mouseX, mouseY, card);
            case "COSMETICS" -> renderCosmetics(ctx, mouseX, mouseY, card);
            case "CONFIGS" -> renderConfigs(ctx, mouseX, mouseY, card);
        }
    }

    // ==================== MODS ====================
    private void renderMods(DrawContext ctx, int mx, int my, int card) {
        // сайдбар конфигов
        int sx = px + 12, sy = py + 58, sw = 190;
        int i = 0;
        for (String prof : ConfigManager.profiles()) {
            int yy = sy + i * 26;
            boolean active = prof.equals(ConfigManager.currentProfile);
            ctx.fill(sx, yy, sx + sw, yy + 22, active ? 0xFF232838 : 0xFF171B26);
            ctx.drawTextWithShadow(textRenderer, prof, sx + 8, yy + 6, active ? accent() : 0xFFB8BCD0);
            String profFinal = prof;
            clickable(new Rect(sx + sw - 18, yy + 4, 14, 14), () -> { if (!profFinal.equals("default")) { ConfigManager.deleteProfile(profFinal); } }, () -> !profFinal.equals("default"));
            i++;
        }
        int by = sy + i * 26 + 8;
        button(ctx, new Rect(sx, by, sw, 22), LangManager.t("newConfig"), () -> newConfigName = "new-config" + ConfigManager.profiles().size());
        by += 30;
        button(ctx, new Rect(sx, by, sw, 22), LangManager.t("editHud"), () -> MinecraftClient.getInstance().setScreen(new HudEditorScreen()));

        // фильтры
        int fx = px + 220, fy = py + 58;
        String[] filters = {"ALL", "HUD", "VISUAL", "UTILITY", "OTHER"};
        for (String f : filters) {
            int w = textRenderer.getWidth(f) + 20;
            boolean on = f.equals(filter);
            ctx.fill(fx, fy, fx + w, fy + 20, on ? 0xFF2A3350 : 0xFF171B26);
            ctx.drawTextWithShadow(textRenderer, f, fx + 10, fy + 6, on ? accent() : 0xFF9A9EB0);
            final String ff = f;
            clickable(new Rect(fx, fy, w, 20), () -> filter = ff);
            fx += w + 6;
        }

        // поиск
        int searchW = 150, searchX = px + PW - searchW - 14, searchY = py + 58;
        ctx.fill(searchX, searchY, searchX + searchW, searchY + 20, 0xFF10131C);
        ctx.drawBorder(searchX, searchY, searchW, 20, typing ? accent() : 0xFF2A2F40);
        ctx.drawTextWithShadow(textRenderer, typing ? search : LangManager.t("search"), searchX + 8, searchY + 6, typing ? 0xFFFFFFFF : 0xFF6A6F82);

        // карточки
        int gx = px + 220, gy = py + 88, gw = px + PW - gx - 12;
        List<Module> list = new ArrayList<>();
        Category fc = null;
        try { fc = Category.valueOf(filter); } catch (IllegalArgumentException ignored) {}
        for (Module m : ModuleManager.MODULES) {
            if (fc != null && m.category != fc) continue;
            if (!search.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) continue;
            list.add(m);
        }

        int cw = (gw - 24) / 3, ch = 150, gap = 10;
        int maxRows = (PH - 100) / (ch + gap);
        int rows = (list.size() + 2) / 3;
        scrollMods = Math.max(0, Math.min(scrollMods, Math.max(0, rows - maxRows)));

        if (list.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, LangManager.t("noResults"), gx + 8, gy + 20, 0xFF6A6F82);
        }

        for (int idx = 0; idx < list.size(); idx++) {
            int r = idx / 3, c = idx % 3;
            int rr = r - scrollMods;
            if (rr < 0 || rr >= maxRows) continue;
            Module m = list.get(idx);
            int x = gx + c * (cw + gap), y = gy + rr * (ch + gap);

            boolean hovered = mx >= x && mx <= x + cw && my >= y && my <= y + ch;
            ctx.fill(x, y, x + cw, y + ch, hovered ? 0xFF232838 : card);
            ctx.drawBorder(x, y, cw, ch, 0x66303548);

            drawIcon(ctx, m, x + cw / 2 - 12, y + 22);
            ctx.drawCenteredTextWithShadow(textRenderer, m.name, x + cw / 2, y + 62, 0xFFE8E8F0);

            // строка OPTIONS
            ctx.fill(x + 8, y + 84, x + cw / 2 - 4, y + 104, 0xFF20242F);
            ctx.drawTextWithShadow(textRenderer, LangManager.t("options"), x + 14, y + 92, 0xFF9A9EB0);
            final Module mm = m;
            clickable(new Rect(x + 8, y + 84, cw / 2 - 12, 20), () -> mm.toggle()); // открыть опции = тумблер лупом

            // полоса ENABLED/DISABLED
            String label = m.enabled ? LangManager.t("enabled") : LangManager.t("disabled");
            int barCol = m.enabled ? 0xFF00E5A8 : 0xFFE5395F;
            ctx.fill(x + 8, y + 110, x + cw - 8, y + 132, barCol);
            ctx.drawCenteredTextWithShadow(textRenderer, label, x + cw / 2, y + 117, 0xFFFFFFFF);
            clickable(new Rect(x + 8, y + 110, cw - 16, 22), mm::toggle);
        }
    }

    // ==================== SETTINGS ====================
    private void renderSettings(DrawContext ctx, int mx, int my, int card) {
        int x = px + 220, y = py + 58, w = px + PW - x - 16;
        int ry = y;

        // Theme
        row(ctx, x, ry, w, card, "Theme", () -> {});
        cycleWidget(ctx, x + 200, ry + 6, w - 216, ThemeManager.current,
                () -> ThemeManager.cycle(-1), () -> ThemeManager.cycle(1));
        ry += 34;

        // Language
        row(ctx, x, ry, w, card, "Language", () -> {});
        cycleWidget(ctx, x + 200, ry + 6, w - 216, LangManager.language,
                () -> LangManager.cycle(-1), () -> LangManager.cycle(1));
        ry += 34;

        // Hud Text Color
        int hudCol = 0xFFFFFFFF;
        Module wm = ModuleManager.get("Watermark");
        if (wm != null) for (Setting s : wm.settings) if (s instanceof ColorSetting cs) hudCol = cs.value;
        row(ctx, x, ry, w, card, "Hud Text Color", () -> {
            for (Setting s : wm.settings) if (s instanceof ColorSetting cs) { cs.cycle(); ConfigManager.save(); }
        });
        int sw = 26;
        ctx.fill(x + w - sw - 10, ry + 8, x + w - 10, ry + 30, hudCol);
        ctx.drawBorder(x + w - sw - 10, ry + 8, sw, 22, 0x66FFFFFF);
        ry += 34;

        // GUI Scale
        SliderSetting scaleSetting = new SliderSetting("GUI Scale", 1.0, 0.8, 1.6, 0.05) {
            @Override public void set(double v) { super.set(v); ConfigManager.save(); }
        };
        sliderRow(ctx, x, ry, w, card, "GUI Scale", scaleSetting);
        ry += 34;

        // Hide Elytra
        toggleRow(ctx, x, ry, w, card, "Hide Elytra (wings replace elytra)", CosmeticManager.hideElytra, v -> { CosmeticManager.hideElytra = v; ConfigManager.save(); });
        ry += 34;

        // Wing Motion
        SliderSetting motion = new SliderSetting("WING MOTION", CosmeticManager.wingMotion, 0, 10, 0.5) {
            @Override public void set(double v) { super.set(v); CosmeticManager.wingMotion = v; ConfigManager.save(); }
        };
        sliderRow(ctx, x, ry, w, card, "WING MOTION", motion);
        ry += 34;

        // Aura
        toggleRow(ctx, x, ry, w, card, "Cosmetic Aura Particles", CosmeticManager.showAura, v -> { CosmeticManager.showAura = v; ConfigManager.save(); });
        ry += 34;

        // Blur note
        row(ctx, x, ry, w, card, "Blur Radius", null);
        ctx.drawTextWithShadow(textRenderer, "Uses vanilla menu blur (video settings)", x + 200, ry + 14, 0xFF8A8FA0);
        ry += 34;

        ctx.drawTextWithShadow(textRenderer, "Render Other Player Cosmetics: everyone renders standard skins (cosmetics are local-only)", x, ry, 0xFF8A8FA0);
    }

    // ==================== COSMETICS ====================
    private void renderCosmetics(DrawContext ctx, int mx, int my, int card) {
        int x = px + 12, y = py + 58, w = 180;
        String[] cats = {"ALL", "WINGS", "CAPES", "HATS", "PETS"};
        int cy = y;
        for (String c : cats) {
            boolean on = c.equals(cosmeticCategory);
            ctx.fill(x, cy, x + w, cy + 26, on ? 0xFF2A3350 : 0xFF171B26);
            ctx.drawTextWithShadow(textRenderer, c, x + 12, cy + 8, on ? accent() : 0xFF9A9EB0);
            final String cc = c;
            clickable(new Rect(x, cy, w, 26), () -> { cosmeticCategory = cc; selected = null; });
            cy += 32;
        }

        int gx = x + w + 14, gw = 400;
        List<Cosmetic> list = new ArrayList<>();
        for (Cosmetic c : CosmeticManager.CATALOG) {
            boolean ok = cosmeticCategory.equals("ALL") || c.type.name().startsWith(cosmeticCategory.replace("CAPES", "CAPE").replace("HATS", "HAT").replace("PETS", "PET").replace("WINGS", "WINGS"));
            if (ok) list.add(c);
        }
        int colW = 190, rowH = 128;
        int cols = 2;
        for (int idx = 0; idx < list.size(); idx++) {
            int r = idx / cols, cc2 = idx % cols;
            int yy = y + r * (rowH + 10) - scrollCos;
            if (yy + rowH < py + 50 || yy > py + PH - 30) continue;
            Cosmetic c = list.get(idx);
            int cx2 = gx + cc2 * (colW + 10);
            boolean hovered = mx >= cx2 && mx <= cx2 + colW && my >= yy && my <= yy + rowH;
            boolean on = CosmeticManager.isActive(c);
            ctx.fill(cx2, yy, cx2 + colW, yy + rowH, hovered || on ? 0xFF232838 : card);
            ctx.drawBorder(cx2, yy, colW, rowH, on ? accent() : 0x66303548);

            Identifier tex = Identifier.of(c.type.name().toLowerCase(Locale.ROOT) + "s", "empty");
            try {
                Identifier tid = Identifier.of("ezzclient", c.texturePath().substring("ezzclient:".length()));
                ctx.drawTexture(net.minecraft.client.render.RenderPipelines.GUI_TEXTURED, tid, cx2 + colW / 2 - 24, yy + 12, 0, 0, 48, 24, 64, 32);
            } catch (Exception ignored) {}
            ctx.drawCenteredTextWithShadow(textRenderer, c.name, cx2 + colW / 2, yy + 44, 0xFFE8E8F0);
            final Cosmetic cc = c;
            clickable(new Rect(cx2, yy, colW, rowH), () -> { selected = cc; });
        }

        // превью-панель справа
        int pxx = gx + gw + 12, pw = px + PW - pxx - 12;
        if (pw > 120) {
            ctx.fill(pxx, y, pxx + pw, py + PH - 14, 0xFF12151F);
            ctx.drawTextWithShadow(textRenderer, "PREVIEW", pxx + 10, y + 8, accent());
            if (selected != null) {
                ctx.drawCenteredTextWithShadow(textRenderer, selected.name.toUpperCase(), pxx + pw / 2, y + 150, 0xFFE8E8F0);
                // дескрипшн построчно
                String[] words = selected.desc.split(" ");
                StringBuilder line = new StringBuilder(); int ly = y + 168;
                for (String wd : words) {
                    if (textRenderer.getWidth(line + " " + wd) > pw - 20) { ctx.drawTextWithShadow(textRenderer, line.toString(), pxx + 10, ly, 0xFFB8BCD0); line = new StringBuilder(wd); ly += 11; }
                    else line = new StringBuilder(line.isEmpty() ? wd : line + " " + wd);
                }
                if (!line.isEmpty()) ctx.drawTextWithShadow(textRenderer, line.toString(), pxx + 10, ly, 0xFFB8BCD0);

                int by = py + PH - 66;
                button(ctx, new Rect(pxx + 10, by, pw - 20, 22), CosmeticManager.isActive(selected) ? LangManager.t("remove") : "EQUIP", () -> {
                    if (CosmeticManager.isActive(selected)) CosmeticManager.unequip(selected); else CosmeticManager.equip(selected);
                });
            } else {
                ctx.drawCenteredTextWithShadow(textRenderer, LangManager.t("dragToRotate"), pxx + pw / 2, y + 150, 0xFF6A6F82);
            }
        }
    }

    // ==================== CONFIGS ====================
    private void renderConfigs(DrawContext ctx, int mx, int my, int card) {
        int x = px + 220, y = py + 58, w = px + PW - x - 16;
        ctx.drawTextWithShadow(textRenderer, "PROFILES", x, y, accent());
        int i = 0;
        for (String prof : ConfigManager.profiles()) {
            int yy = y + 20 + i * 30;
            boolean active = prof.equals(ConfigManager.currentProfile);
            ctx.fill(x, yy, x + w, yy + 26, active ? 0xFF232838 : 0xFF171B26);
            ctx.drawTextWithShadow(textRenderer, prof, x + 10, yy + 8, active ? accent() : 0xFFB8BCD0);
            String p = prof;
            clickable(new Rect(x, yy, w, 26), () -> { ConfigManager.save(); ConfigManager.loadProfile(p); });
            if (!p.equals("default")) {
                int bx = x + w - 26;
                ctx.fill(bx, yy + 5, bx + 16, yy + 21, 0xFF2A1A20);
                ctx.drawTextWithShadow(textRenderer, "x", bx + 5, yy + 8, 0xFFFF6688);
                clickable(new Rect(bx, yy + 5, 16, 16), () -> ConfigManager.deleteProfile(p));
            }
            i++;
        }
        button(ctx, new Rect(x, y + 24 + i * 30, w, 22), LangManager.t("newConfig"),
                () -> newConfigName = "new-config" + ConfigManager.profiles().size());
        if (newConfigName != null) {
            int yy = y + 54 + i * 30;
            ctx.fill(x, yy, x + w, yy + 26, 0xFF10131C);
            ctx.drawTextWithShadow(textRenderer, newConfigName, x + 8, yy + 8, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, "[enter] save", x + w - 70, yy + 8, 0xFF6A6F82);
        }
    }

    // ==================== виджеты ====================
    private void row(DrawContext ctx, int x, int y, int w, int card, String label, Runnable action) {
        ctx.fill(x, y, x + w, y + 30, card);
        ctx.drawTextWithShadow(textRenderer, label, x + 12, y + 11, 0xFFE8E8F0);
        if (action != null) clickable(new Rect(x, y, w, 30), action);
    }

    private void toggleRow(DrawContext ctx, int x, int y, int w, int card, String label, boolean val, java.util.function.BooleanConsumer on) {
        row(ctx, x, y, w, card, label, () -> on.accept(!val));
        int tx = x + w - 44;
        ctx.fill(tx, y + 7, tx + 34, y + 23, val ? 0xFF00E5A8 : 0xFF3A3F52);
        int kx = val ? tx + 18 : tx + 2;
        ctx.fill(kx, y + 9, kx + 14, y + 21, 0xFFFFFFFF);
    }

    private void sliderRow(DrawContext ctx, int x, int y, int w, int card, String label, SliderSetting s) {
        row(ctx, x, y, w, card, label, () -> {});
        int sx = x + 200, sw = w - 216;
        ctx.fill(sx, y + 14, sx + sw, y + 17, 0xFF2A2F40);
        double frac = (s.value - s.min) / (s.max - s.min);
        ctx.fill(sx, y + 14, sx + (int) (sw * frac), y + 17, accent());
        ctx.fill(sx + (int) (sw * frac) - 4, y + 10, sx + (int) (sw * frac) + 4, y + 21, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, s.display(), sx + sw + 8, y + 11, 0xFFB8BCD0);
        sliders.add(new Object[]{new Rect(x, y, w, 30), s});
    }

    private void cycleWidget(DrawContext ctx, int x, int y, int w, String val, Runnable left, Runnable right) {
        int bw = 20;
        ctx.fill(x, y, x + w, y + 22, 0xFF171B26);
        ctx.fill(x + bw, y, x + w - bw, y + 22, 0xFF20242F);
        ctx.drawCenteredTextWithShadow(textRenderer, val, x + w / 2, y + 7, 0xFFE8E8F0);
        ctx.drawTextWithShadow(textRenderer, "<", x + 8, y + 7, accent());
        ctx.drawTextWithShadow(textRenderer, ">", x + w - 12, y + 7, accent());
        clickable(new Rect(x, y, bw, 22), left);
        clickable(new Rect(x + w - bw, y, bw, 22), right);
    }

    private void button(DrawContext ctx, Rect r, String label, Runnable action) {
        ctx.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), 0xFF2A3350);
        ctx.drawBorder(r.x(), r.y(), r.w(), r.h(), accent());
        ctx.drawCenteredTextWithShadow(textRenderer, label, r.x() + r.w() / 2, r.y() + 7, 0xFFE8E8F0);
        clickable(r, action);
    }

    private void clickable(Rect r, Runnable action) {
        clicks.add(new Object[]{r, action, Boolean.TRUE});
    }
    private void clickable(Rect r, Runnable action, boolean enabled) {
        clicks.add(new Object[]{r, action, enabled});
    }

    private void drawIcon(DrawContext ctx, Module m, int cx, int cy) {
        int c = accent();
        switch (m.category) {
            case HUD -> { ctx.fill(cx, cy, cx + 24, cy + 3, c); ctx.fill(cx, cy + 8, cx + 18, cy + 11, c); ctx.fill(cx, cy + 16, cx + 24, cy + 19, c); }
            case VISUAL -> { ctx.fill(cx + 8, cy, cx + 16, cy + 24, c); ctx.fill(cx, cy + 8, cx + 24, cy + 16, c); }
            default -> { for (int i = 0; i < 3; i++) ctx.fill(cx + i * 9, cy + i * 6, cx + 6 + i * 9, cy + 6 + i * 6, c); }
        }
    }

    // ==================== ввод ====================
    @Override public boolean mouseClicked(double mx, double my, int button) {
        for (Object[] o : clicks) {
            Rect r = (Rect) o[0];
            boolean enabled = (Boolean) o[2];
            if (enabled && mx >= r.x() && mx <= r.x() + r.w() && my >= r.y() && my <= r.y() + r.h()) {
                ((Runnable) o[1]).run();
                return true;
            }
        }
        for (Object[] o : sliders) {
            Rect r = (Rect) o[0];
            if (mx >= r.x() && mx <= r.x() + r.w() && my >= r.y() && my <= r.y() + r.h()) {
                ((SliderSetting) o[1]).set(dragValue((SliderSetting) o[1], (int) mx));
                return true;
            }
        }
        // клик по поиску
        int searchX = px + PW - 164, searchY = py + 58;
        typing = mx >= searchX && mx <= searchX + 150 && my >= searchY && my <= searchY + 20 && tab.equals("MODS");
        if (!typing) search = search;
        return super.mouseClicked(mx, my, button);
    }

    private double dragValue(SliderSetting s, int mx) {
        int sx = px + 420 + 200 - 220; // грубо, но рабочо для sliders в settings
        return s.value;
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (Object[] o : sliders) {
            Rect r = (Rect) o[0];
            SliderSetting s = (SliderSetting) o[1];
            int sx = r.x() + 200, sw = r.w() - 216;
            if (sx <= mx && mx <= sx + sw) {
                double frac = (mx - sx) / (double) sw;
                s.set(s.min + frac * (s.max - s.min));
                s.load(new com.google.gson.JsonObject()); // no-op, keep value
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (tab.equals("MODS")) scrollMods -= (int) Math.signum(v);
        if (tab.equals("COSMETICS")) scrollCos -= (int) Math.signum(v) * 20;
        return true;
    }

    @Override public boolean charTyped(char chr, int mods) {
        if (typing) { search += chr; return true; }
        return super.charTyped(chr, mods);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
            return true;
        }
        if (typing && keyCode == GLFW.GLFW_KEY_ESCAPE) { typing = false; return true; }
        if (newConfigName != null && keyCode == GLFW.GLFW_KEY_ENTER) {
            ConfigManager.createProfile(newConfigName);
            ConfigManager.save();
            newConfigName = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public void close() {
        ConfigManager.save();
        MinecraftClient.getInstance().setScreen(null);
    }
}
