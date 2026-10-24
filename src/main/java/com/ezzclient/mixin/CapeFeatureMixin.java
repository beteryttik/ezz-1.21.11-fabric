package com.ezzclient.mixin;

import com.ezzclient.cosmetics.CosmeticManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Если надет наш кап — ванильный плащ не рисуем (даже если он есть у игрока). */
@Mixin(net.minecraft.client.render.entity.feature.CapeFeatureRenderer.class)
public class CapeFeatureMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ezz$hideVanillaCape(net.minecraft.client.render.entity.state.EntityRenderState state,
                                     net.minecraft.client.util.math.MatrixStack ms,
                                     net.minecraft.client.render.VertexConsumerProvider vcp,
                                     int light, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (CosmeticManager.activeCape != null && CosmeticManager.visibleFor(state.id)) ci.cancel();
    }
}
