package com.ezzclient.mixin;

import com.ezzclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Прячем ванильный прицел, когда включен Crosshair Custom. */
@Mixin(InGameHud.class)
public class CrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void ezz$customCrosshair(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick, CallbackInfo ci) {
        Module m = ModuleManager.get("Crosshair Custom");
        if (m != null && m.enabled) ci.cancel();
    }
}
