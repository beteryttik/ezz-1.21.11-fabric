package com.ezzclient.mixin;

import com.ezzclient.cosmetics.CosmeticManager;
import com.ezzclient.cosmetics.render.WingsModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Замена элитр на крылья: отменяем ванильную отрисовку элитр
 * и рисуем наши крылья на их месте. Только для локального игрока.
 */
@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void ezz$replaceElytra(net.minecraft.client.render.entity.state.EntityRenderState state,
                                   MatrixStack ms, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !(state instanceof BipedEntityRenderState)) return;
        if (!CosmeticManager.hideElytra || CosmeticManager.activeWings == null) return;
        if (!CosmeticManager.visibleFor(state.id)) return;

        ci.cancel();

        WingsModel model = WingsFeatureHolder.get();
        if (model == null) return;
        BipedEntityRenderState s = (BipedEntityRenderState) state;
        float speed = mc.player.fallDistance > 0.2f || mc.player.isGliding() ? 0.6f : 0.1f;
        Identifier tex = net.minecraft.util.Identifier.of("ezzclient",
                "textures/cosmetic/wings/" + CosmeticManager.activeWings + ".png");
        ms.push();
        WingsModel.translateToBack(ms, s.bodyYaw, s.height);
        model.render(ms, vcp, tex, s.age + mc.getRenderTime(), speed,
                CosmeticManager.wingMotion, mc.player.isGliding(), light);
        ms.pop();
    }
}
