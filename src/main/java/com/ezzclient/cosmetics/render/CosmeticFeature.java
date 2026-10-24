package com.ezzclient.cosmetics.render;

import com.ezzclient.cosmetics.Cosmetic;
import com.ezzclient.cosmetics.CosmeticManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Рисует шляпы и капы поверх стандартной модели игрока.
 * Видно ТОЛЬКО локальному игроку (проверка в visibleFor).
 */
public class CosmeticFeature extends FeatureRenderer<PlayerEntityRenderState> {
    private final HatModel hatModel;
    private final Identifier hatBase = Identifier.of("ezzclient", "textures/cosmetic/hats/crown.png");

    public CosmeticFeature(FeatureRendererContext<PlayerEntityRenderState, ?> ctx, net.minecraft.client.render.entity.model.EntityModelLoader loader) {
        super(ctx);
        this.hatModel = new HatModel(loader.createModel(net.minecraft.client.render.entity.model.EntityModelLayers.PLAYER));
    }

    @Override
    public void render(PlayerEntityRenderState state, MatrixStack ms, VertexConsumerProvider vcp, int light) {
        if (!CosmeticManager.visibleFor(state.id)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (CosmeticManager.activeHat != null) renderHat(state, ms, vcp, light);
        if (CosmeticManager.activeCape != null) renderCape(state, ms, vcp, light);
    }

    private void renderHat(PlayerEntityRenderState state, MatrixStack ms, VertexConsumerProvider vcp, int light) {
        ms.push();
        // позиция головы (yaw/pitch из рендер-стейта)
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.bodyYaw + 180.0f));
        ms.translate(0, state.height - 0.1f, 0);
        HatModel.setAngleFor(hatModel.head, state.pitch);
        Identifier tex = Identifier.of("ezzclient", "textures/cosmetic/hats/" + CosmeticManager.activeHat + ".png");
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(tex));
        hatModel.head.render(ms, vc, light, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }

    private void renderCape(PlayerEntityRenderState state, MatrixStack ms, VertexConsumerProvider vcp, int light) {
        ms.push();
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.bodyYaw));
        ms.translate(0, state.height - 1.35f, 0.08f);
        float sway = MathHelper.sin((state.age + mc.getRenderTime()) * 0.06f) * 0.03f;
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sway * 10f));
        Identifier tex = Identifier.of("ezzclient", "textures/cosmetic/capes/" + CosmeticManager.activeCape + ".png");
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(tex));
        CapeModel.renderPlane(ms, vc, light, 0.6f, 0.98f, tex);
        ms.pop();
    }

    static class HatModel {
        public final net.minecraft.client.model.ModelPart head;
        HatModel(net.minecraft.client.model.ModelPart base) {
            this.head = base.getChild("head");
        }
        static void setAngleFor(net.minecraft.client.model.ModelPart part, float pitch) {
            part.pivotX = 0; part.pivotY = 24; part.pivotZ = 0;
            part.pitch = -MathHelper.clamp(pitch / 85f, -1f, 1f) * 0.5f;
        }
    }

    static class CapeModel {
        static void renderPlane(MatrixStack ms, VertexConsumer vc, int light, float w, float h, Identifier tex) {
            MatrixStack.Entry e = ms.peek();
            float u0 = 0, u1 = 1, v0 = 0, v1 = 1;
            e.getNormalMatrix();
            vc.vertex(e, 0, -h, 0).texture(u0, v0).color(255,255,255,255).normal(e, 0, 0, 1);
            vc.vertex(e, 0, -h, w).texture(u0, v1).color(255,255,255,255).normal(e, 0, 0, 1);
            vc.vertex(e, 0, 0, w).texture(u1, v1).color(255,255,255,255).normal(e, 0, 0, 1);
            vc.vertex(e, 0, 0, 0).texture(u1, v0).color(255,255,255,255).normal(e, 0, 0, 1);
        }
    }
}
