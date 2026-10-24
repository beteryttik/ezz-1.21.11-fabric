package com.ezzclient;

import com.ezzclient.config.ConfigManager;
import com.ezzclient.cosmetics.CosmeticManager;
import com.ezzclient.cosmetics.render.PetRenderer;
import com.ezzclient.gui.ClickGUIScreen;
import com.ezzclient.module.ModuleManager;
import com.ezzclient.module.HudTracker;
import com.ezzclient.modules.AllModules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class EzzClient implements ClientModInitializer {
    public static final String MOD_ID = "ezzclient";
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        CosmeticManager.init();
        PetRenderer.init();
        ConfigManager.load();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ezzclient.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "Ezz Client"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUIScreen());
            }
            ModuleManager.onTick(mc);
            CosmeticManager.tickParticles(mc);
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ModuleManager.onHud(mc, ctx, tick);
        });

        // чат: авто-GG и фильтр спама
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String msg = message.getString();
            for (com.ezzclient.module.Module m : ModuleManager.MODULES) {
                if (m instanceof AllModules.AutoGG gg && gg.tryGg(msg)) return false;
                if (m instanceof AllModules.ChatFilter cf && cf.shouldHide(msg)) return false;
            }
            return true;
        });

        // CPS-трекер
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            long h = mc.getWindow().getHandle();
            boolean l = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean r = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            HudTracker.poll(l, r);
        });

        // TODO: рендер питомцев. Старое WorldRenderEvents.AFTER_ENTITIES удалено в 1.21.9,
        // новое API (LevelRenderEvents) требует переработки PetRenderer под фазы extraction/drawing.
        // Вернём подписку после правки PetRenderer.java.
        // LevelRenderEvents.?.register(PetRenderer::render);
    }
}
