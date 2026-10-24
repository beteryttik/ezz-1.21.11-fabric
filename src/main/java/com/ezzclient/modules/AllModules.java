package com.ezzclient.modules;

import com.ezzclient.module.*;
import com.ezzclient.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.regex.Pattern;

/** Все 30 модулей клиента. Каждый — маленький класс. */
public final class AllModules {
    private static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    // ---------- HUD ----------

    public static class Watermark extends Module {
        private final SliderSetting x = slider("X", 4, 0, 800, 1);
        private final SliderSetting y = slider("Y", 4, 0, 400, 1);
        public Watermark() { super("Watermark", "Shows the Ezz Client watermark", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            String s = "ezz client 2.0";
            int w = mc.textRenderer.getWidth(s) + 10;
            ctx.fill(x.value, y.value, x.value + w, y.value + 16, 0xB0101018);
            ctx.fill(x.value, y.value + 15, x.value + w, y.value + 16, 0xFF00E5A8);
            ctx.drawTextWithShadow(mc.textRenderer, s, x.value + 5, y.value + 4, 0xFF00E5A8);
        }
    }

    public static class FPSDisplay extends Module {
        public FPSDisplay() { super("FPS", "Shows current FPS", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            String s = mc.getCurrentFps() + " FPS";
            ctx.drawTextWithShadow(mc.textRenderer, s, 6, 22, 0xFFFFFF66);
        }
    }

    public static class Coordinates extends Module {
        private final ModeSetting extra = mode("Extra", "XYZ only", "XYZ+Biome", "XYZ+Biome+Facing");
        public Coordinates() { super("Coordinates", "Shows your position", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (mc.player == null) return;
            String s = String.format(Locale.ROOT, "XYZ: %.0f, %.0f, %.0f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (extra.index >= 1) s += "  " + mc.world.getBiome(mc.player.getBlockPos()).getKey()
                    .map(k -> k.getValue().getPath()).orElse("unknown");
            if (extra.index >= 2) s += "  [" + mc.player.getHorizontalFacing().name() + "]";
            ctx.drawTextWithShadow(mc.textRenderer, s, 6, 36, 0xFFCCCCCC);
        }
    }

    public static class PingDisplay extends Module {
        public PingDisplay() { super("Ping", "Shows your ping", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (mc.getNetworkHandler() == null || mc.player == null) return;
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = e != null ? e.getLatency() : 0;
            ctx.drawTextWithShadow(mc.textRenderer, ping + " ms", 6, 50, 0xFF88CCFF);
        }
    }

    public static class CPSCounter extends Module {
        public CPSCounter() { super("CPS Counter", "Shows clicks per second", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            String s = HudTracker.cpsL() + " | " + HudTracker.cpsR() + " CPS";
            ctx.drawTextWithShadow(mc.textRenderer, s, 6, 64, 0xFFFFAA55);
        }
    }

    public static class Keystrokes extends Module {
        public Keystrokes() { super("Keystrokes", "Shows pressed keys WASD + Space", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            long h = mc.getWindow().getHandle();
            int x = 6, y = 78, s = 16, gap = 2;
            key(ctx, x + s + gap, y, s, GLFW.glfwGetKey(h, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS, "W");
            key(ctx, x, y + s + gap, s, GLFW.glfwGetKey(h, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS, "A");
            key(ctx, x + s + gap, y + s + gap, s, GLFW.glfwGetKey(h, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS, "S");
            key(ctx, x + 2 * (s + gap), y + s + gap, s, GLFW.glfwGetKey(h, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS, "D");
            key(ctx, x, y + 2 * (s + gap), 3 * s + 2 * gap, GLFW.glfwGetKey(h, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS, "SPACE");
        }
        private void key(DrawContext ctx, int x, int y, int s, boolean on, String label) {
            ctx.fill(x, y, x + s, y + s, on ? 0xE000E5A8 : 0xB0101018);
            ctx.drawTextWithShadow(mc().textRenderer, label, x + s / 2 - mc().textRenderer.getWidth(label) / 2, y + s / 2 - 4, 0xFFFFFFFF);
        }
    }

    public static class ArmorHud extends Module {
        public ArmorHud() { super("Armor HUD", "Shows your armor and held item", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (mc.player == null) return;
            int x = 6, y = 140;
            for (ItemStack st : mc.player.getInventory().armor) drawItem(ctx, st, x, y, 18);
            drawItem(ctx, mc.player.getInventory().getMainHandStack(), x + 96, y, 18);
        }
        private void drawItem(DrawContext ctx, ItemStack st, int x, int y, int size) {
            if (st.isEmpty()) return;
            ctx.drawItem(st, x, y);
            ctx.drawStackOverlay(mc().textRenderer, st, x, y);
        }
    }

    public static class HealthIndicator extends Module {
        public HealthIndicator() { super("Health Indicator", "Shows exact health", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (mc.player == null) return;
            float hp = mc.player.getHealth();
            String s = String.format(Locale.ROOT, "HP: %.1f", hp);
            ctx.drawTextWithShadow(mc.textRenderer, s, 6, 170, hp > 10 ? 0xFF55FF55 : 0xFFFF5555);
        }
    }

    public static class PotionList extends Module {
        public PotionList() { super("Potion List", "Lists active effects with time", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (mc.player == null) return;
            int y = 190;
            for (StatusEffectInstance eff : mc.player.getStatusEffects()) {
                String name = eff.getEffectType().value().getName().getString();
                int sec = eff.getDuration() / 20;
                String line = name + " " + (sec / 60) + ":" + String.format("%02d", sec % 60);
                ctx.drawTextWithShadow(mc.textRenderer, line, 6, y, 0xFFCC88FF);
                y += 11;
            }
        }
    }

    public static class Minimap extends Module {
        private final SliderSetting size = slider("Size", 96, 64, 160, 16);
        private final SliderSetting zoom = slider("Zoom (radius)", 24, 8, 64, 8);
        private int[][] grid = null; private int gx = 0, gy = 0; private long last = 0;

        public Minimap() { super("Minimap", "Simple map around the player", Category.HUD); }

        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null || mc.world == null) return;
            if (System.currentTimeMillis() - last < 400) return;
            last = System.currentTimeMillis();
            int r = (int) zoom.value;
            int d = 2 * r + 1;
            grid = new int[d][d];
            BlockPos base = mc.player.getBlockPos();
            for (int dz = -r; dz <= r; dz++)
                for (int dx = -r; dx <= r; dx++) {
                    BlockPos p = new BlockPos(base.getX() + dx, Math.max(mc.world.getBottomY(), base.getY() - 2), base.getZ() + dz);
                    BlockState st = mc.world.getBlockState(p);
                    grid[dz + r][dx + r] = 0xFF000000 | st.getMapColor(mc.world, p).color;
                }
            gx = base.getX(); gy = base.getZ(); // snapshot coords
        }

        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (grid == null || mc.player == null) return;
            int s = (int) size.value, r = (int) zoom.value;
            int d = 2 * r + 1, cell = s / d;
            int x = mc.getWindow().getScaledWidth() - s - 8, y = 8;
            ctx.fill(x - 2, y - 2, x + s + 2, y + s + 2, 0xB0101018);
            for (int dz = 0; dz < d; dz++)
                for (int dx = 0; dx < d; dx++) {
                    int px = x + dx * cell, py = y + dz * cell;
                    ctx.fill(px, py, px + cell, py + cell, grid[dz][dx]);
                }
            int cx = x + s / 2, cy = y + s / 2;
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFF5555);
        }
    }

    public static class FakeFPS extends Module {
        private final SliderSetting val = slider("Fake FPS", 120, 1, 3000, 1);
        public FakeFPS() { super("Fake FPS", "Just a big number in the HUD. For flexing.", Category.HUD); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            ctx.drawTextWithShadow(mc.textRenderer, (int) val.value + " FPS", 6, 184, 0xFFFFFF99);
        }
    }

    // ---------- VISUAL ----------

    public static class FullBright extends Module {
        private double prev = 1.0;
        public FullBright() { super("Full Bright", "Maximum brightness", Category.VISUAL); }
        @Override protected void onEnable() {
            prev = mc().options.getGamma().getValue();
            mc().options.getGamma().setValue(16.0);
        }
        @Override protected void onDisable() { mc().options.getGamma().setValue(prev); }
    }

    public static class Zoom extends Module {
        private Integer prev = null;
        private final SliderSetting factor = slider("Factor", 3.0, 1.5, 8.0, 0.5);
        public Zoom() { super("Zoom", "Press Z to zoom in", Category.VISUAL); }
        @Override public void onTick(MinecraftClient mc) {
            boolean held = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS;
            if (held && prev == null) { prev = mc.options.getFov().getValue(); }
            if (!held && prev != null) { mc.options.getFov().setValue(prev); prev = null; }
            if (held && prev != null) mc.options.getFov().setValue(Math.max(10, (int) (prev / factor.value)));
        }
    }

    public static class CustomSkyColor extends Module {
        private final ModeSetting sky = mode("Sky", "Pink Sunset", "Endless Night", "Deep Blue", "Blood Moon");
        public CustomSkyColor() { super("Custom Sky Color", "Tints the sky for you (client-side)", Category.VISUAL); }
        public int color() {
            return switch (sky.index) {
                case 0 -> 0xFFFF9EC0; case 1 -> 0xFF141830; case 2 -> 0xFF0A2A66; default -> 0xFF661010;
            };
        }
        public boolean active() { return enabled; }
    }

    public static class WorldParticles extends Module {
        private final ModeSetting kind = mode("Particle", "End Rod", "Flame", "Happy Villager", "Enchant", "Spark");
        private final SliderSetting rate = slider("Rate", 2, 1, 8, 1);
        public WorldParticles() { super("World Particles", "Ambient particles around you (only you see them)", Category.VISUAL); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null || mc.world == null) return;
            if (mc.player.age % (int) Math.max(1, 6 - rate.value) != 0) return;
            var type = switch (kind.index) {
                case 0 -> ParticleTypes.END_ROD; case 1 -> ParticleTypes.FLAME;
                case 2 -> ParticleTypes.HAPPY_VILLAGER; case 3 -> ParticleTypes.ENCHANT; default -> ParticleTypes.ELECTRIC_SPARK;
            };
            for (int i = 0; i < rate.value; i++)
                mc.world.addParticle(type,
                        mc.player.getX() + (Math.random() - 0.5), mc.player.getY() + Math.random() * 2, mc.player.getZ() + (Math.random() - 0.5),
                        0, 0.02, 0);
        }
    }

    public static class Animations extends Module {
        private final ModeSetting anim = mode("Style", "Smooth Camera", "Bouncy Hands", "Old Swing");
        public Animations() { super("Animations", "Custom animation pack (client-side)", Category.VISUAL); }
        @Override protected void onEnable() {
            if (anim.index == 0) mc().options.getSmoothCameraEnabled().setValue(true);
        }
        @Override protected void onDisable() {
            if (anim.index == 0) mc().options.getSmoothCameraEnabled().setValue(false);
        }
        @Override public void onTick(MinecraftClient mc) {
            if (anim.index == 0 && !mc.options.getSmoothCameraEnabled().getValue()) mc.options.getSmoothCameraEnabled().setValue(true);
        }
    }

    public static class OldAnimations extends Module {
        public OldAnimations() { super("Old Animations", "1.8-style item swings (feel)", Category.VISUAL); }
    }

    public static class MotionBlur extends Module {
        public MotionBlur() { super("Motion Blur", "Subtle motion blur at high speed (experimental)", Category.VISUAL); }
    }

    public static class CrosshairCustom extends Module {
        private final ColorSetting col = color("Color", 0xFF00E5A8);
        private final SliderSetting thick = slider("Thickness", 1, 1, 4, 1);
        private final SliderSetting len = slider("Length", 5, 2, 12, 1);
        private final BooleanSetting dot = bool("Dot", true);
        public CrosshairCustom() { super("Crosshair Custom", "Custom crosshair", Category.VISUAL); }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
            int cx = w / 2, cy = h / 2, L = (int) len.value, T = (int) thick.value;
            ctx.fill(cx - L, cy - T / 2, cx - 2, cy + T / 2 + (T % 2), col.value);
            ctx.fill(cx + 2, cy - T / 2, cx + L, cy + T / 2 + (T % 2), col.value);
            ctx.fill(cx - T / 2, cy - L, cx + T / 2 + (T % 2), cy - 2, col.value);
            ctx.fill(cx - T / 2, cy + 2, cx + T / 2 + (T % 2), cy + L, col.value);
            if (dot.value) ctx.fill(cx, cy, cx + 1, cy + 1, col.value);
        }
    }

    public static class NametagCustom extends Module {
        private final BooleanSetting outline = bool("Outlined text", true);
        public NametagCustom() { super("Nametag Custom", "Cleaner nametags for you", Category.VISUAL); }
    }

    public static class TimeChanger extends Module {
        private final ModeSetting time = mode("Time", "Client Day", "Client Night");
        public TimeChanger() { super("Time Changer", "Changes perceived time of day for you only (experimental)", Category.VISUAL); }
    }

    // ---------- UTILITY ----------

    public static class AutoWalk extends Module {
        public AutoWalk() { super("Auto Walk", "Walks forward while enabled", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null || mc.currentScreen != null) return;
            mc.options.forwardKey.setPressed(true);
        }
        @Override protected void onDisable() {
            MinecraftClient mc = mc(); if (mc.player != null) mc.options.forwardKey.setPressed(false);
        }
    }

    public static class AutoSprint extends Module {
        public AutoSprint() { super("Auto Sprint", "Always sprint when moving", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null || mc.currentScreen != null) return;
            if (mc.options.forwardKey.isPressed()) mc.options.sprintKey.setPressed(true);
        }
    }

    public static class AutoEat extends Module {
        private final SliderSetting hunger = slider("Eat below hunger", 15, 1, 19, 1);
        private boolean eating = false;
        public AutoEat() { super("Auto Eat", "Automatically eats when hungry", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null || mc.currentScreen != null) return;
            int food = mc.player.getHungerManager().getFoodLevel();
            if (!eating && food < (int) hunger.value) {
                int best = -1;
                for (int i = 0; i < 9; i++) {
                    ItemStack st = mc.player.getInventory().getStack(i);
                    if (!st.isEmpty() && st.contains(net.minecraft.component.DataComponentTypes.FOOD)) { best = i; break; }
                }
                if (best >= 0) {
                    mc.player.getInventory().selectedSlot = best;
                    eating = true;
                }
            }
            if (eating) {
                boolean stillFood = mc.player.getMainHandStack().contains(net.minecraft.component.DataComponentTypes.FOOD);
                if (!stillFood || food >= 20) { eating = false; mc.options.useKey.setPressed(false); return; }
                mc.options.useKey.setPressed(true);
            }
        }
        @Override protected void onDisable() { eating = false; mc().options.useKey.setPressed(false); }
    }

    public static class AutoGG extends Module {
        private static final Pattern WIN = Pattern.compile("(?i).*(victory|you (won|win)|winner|game over).*");
        private long lastGg = 0;
        public AutoGG() { super("Auto GG", "Says gg when the game ends", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) { /* handled via chat hook */ }
        public boolean tryGg(String message) {
            if (!enabled) return false;
            if (WIN.matcher(message).matches() && System.currentTimeMillis() - lastGg > 8000 && mc().player != null) {
                lastGg = System.currentTimeMillis();
                mc().player.networkHandler.sendChatMessage("gg");
                HudToasts.push("[ezz] gg sent!");
            }
            return false;
        }
    }

    public static class AutoRespawn extends Module {
        public AutoRespawn() { super("Auto Respawn", "Respawns instantly on death", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player != null && mc.player.isDead()) mc.player.requestRespawn();
        }
    }

    public static class DeathCoords extends Module {
        private double dx, dy, dz; private boolean has;
        public DeathCoords() { super("Death Coords", "Shows where you died", Category.UTILITY); }
        @Override public void onTick(MinecraftClient mc) {
            if (mc.player == null) return;
            if (mc.player.isDead() && !has) {
                has = true;
                dx = mc.player.getX(); dy = mc.player.getY(); dz = mc.player.getZ();
                HudToasts.push("[ezz] Death at: " + (int) dx + " " + (int) dy + " " + (int) dz);
            } else if (!mc.player.isDead() && mc.player.getHealth() > 0) has = false;
        }
        @Override public void onHud(MinecraftClient mc, DrawContext ctx, RenderTickCounter t) {
            if (!has) return;
            String s = String.format(Locale.ROOT, "Death: %.0f %.0f %.0f", dx, dy, dz);
            ctx.drawTextWithShadow(mc.textRenderer, s, 6, 204, 0xFFFF8888);
        }
    }

    public static class Notifications extends Module {
        public Notifications() { super("Notifications", "Clean popups instead of chat spam", Category.UTILITY); }
    }

    public static class DiscordRPC extends Module {
        public DiscordRPC() { super("Discord RPC", "Shows playing status in Discord (stub, needs lib)", Category.UTILITY); }
    }

    public static class ChatFilter extends Module {
        private final ModeSetting level = mode("Filter", "Mild", "Aggressive");
        public ChatFilter() { super("Chat Filter", "Hides spam in chat", Category.UTILITY); }
        public boolean shouldHide(String msg) {
            if (!enabled) return false;
            String m = msg.toLowerCase();
            if (level.index == 0) return m.contains("free rank") || m.contains("discord.gg");
            return m.contains("free") || m.contains("vote") || m.contains("buy rank") || m.contains("discord.gg") || m.contains("ad:");
        }
    }

    public static class Performance extends Module {
        private final BooleanSetting noClouds = bool("Disable clouds", true);
        private final BooleanSetting fewParticles = bool("Reduce particles", true);
        private final SliderSetting dist = slider("Render distance cap", 12, 4, 32, 1);
        private int prevDist = -1; private ParticlesMode prevPart = null; private CloudRenderMode prevCloud = null;
        public Performance() { super("Performance", "FPS boost: clouds, particles, render distance", Category.UTILITY); }
        @Override protected void onEnable() {
            MinecraftClient mc = mc();
            if (noClouds.value) { prevCloud = mc.options.getCloudRenderModeValue(); mc.options.setCloudRenderModeValue(CloudRenderMode.OFF); }
            if (fewParticles.value) { prevPart = mc.options.getParticlesMode().getValue(); mc.options.getParticlesMode().setValue(ParticlesMode.MINIMAL); }
            prevDist = mc.options.getViewDistance().getValue();
            if (prevDist > (int) dist.value) mc.options.getViewDistance().setValue((int) dist.value);
        }
        @Override protected void onDisable() {
            MinecraftClient mc = mc();
            if (prevCloud != null) { mc.options.setCloudRenderModeValue(prevCloud); prevCloud = null; }
            if (prevPart != null) { mc.options.getParticlesMode().setValue(prevPart); prevPart = null; }
            if (prevDist > 0) { mc.options.getViewDistance().setValue(prevDist); prevDist = -1; }
        }
    }

    public static class HitRange extends Module {
        private HitRange() { super("Hit Range", "Extended attack reach (disabled by policy)", Category.OTHER); }
    }

    // ---------- регистрация ----------

    public static void registerAll() {
        ModuleManager.register(new Watermark());
        ModuleManager.register(new FPSDisplay());
        ModuleManager.register(new Coordinates());
        ModuleManager.register(new PingDisplay());
        ModuleManager.register(new CPSCounter());
        ModuleManager.register(new Keystrokes());
        ModuleManager.register(new ArmorHud());
        ModuleManager.register(new HealthIndicator());
        ModuleManager.register(new PotionList());
        ModuleManager.register(new Minimap());
        ModuleManager.register(new FakeFPS());
        ModuleManager.register(new FullBright());
        ModuleManager.register(new Zoom());
        ModuleManager.register(new CustomSkyColor());
        ModuleManager.register(new WorldParticles());
        ModuleManager.register(new Animations());
        ModuleManager.register(new OldAnimations());
        ModuleManager.register(new MotionBlur());
        ModuleManager.register(new CrosshairCustom());
        ModuleManager.register(new NametagCustom());
        ModuleManager.register(new TimeChanger());
        ModuleManager.register(new AutoWalk());
        ModuleManager.register(new AutoSprint());
        ModuleManager.register(new AutoEat());
        ModuleManager.register(new AutoGG());
        ModuleManager.register(new AutoRespawn());
        ModuleManager.register(new DeathCoords());
        ModuleManager.register(new Notifications());
        ModuleManager.register(new DiscordRPC());
        ModuleManager.register(new ChatFilter());
        ModuleManager.register(new Performance());
    }

    public static class HudTracker {
        private static final Deque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
        private static boolean prevL = false, prevR = false;
        public static void poll(boolean nowL, boolean nowR) {
            if (nowL && !prevL) { left.addLast(System.currentTimeMillis()); if (left.size() > 50) left.removeFirst(); }
            if (nowR && !prevR) { right.addLast(System.currentTimeMillis()); if (right.size() > 50) right.removeFirst(); }
            prevL = nowL; prevR = nowR;
        }
        public static int cpsL() { return count(left); }
        public static int cpsR() { return count(right); }
        private static int count(Deque<Long> q) {
            long now = System.currentTimeMillis(); int n = 0;
            for (long t : q) if (now - t < 1000) n++;
            return n;
        }
    }
}
