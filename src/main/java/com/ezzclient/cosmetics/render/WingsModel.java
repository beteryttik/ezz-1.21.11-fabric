package com.ezzclient.cosmetics.render;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Крылья: рисуются ВМЕСТО элитр (элитра отменяется миксином при hideElytra).
 * Анимация маха привязана к времени и состоянию полёта.
 */
public class WingsModel {
    public final ModelPart root, left, right;

    public WingsModel(ModelPart root) {
        this.root = root;
        this.left = root.getChild("leftWing");
        this.right = root.getChild("rightWing");
    }

    public static TexturedModelData build() {
        ModelData md = new ModelData();
        ModelPartData rootData = md.getRoot();
        rootData.addChild("leftWing", ModelPartBuilder.create()
                .uv(0, 0).mirrored()
                .cuboid(-24.0f, -2.0f, -2.0f, 24.0f, 4.0f, 6.0f),
            PivotPose.of(2.0f, 1.5f, 0.0f));
        rootData.addChild("rightWing", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(0.0f, -2.0f, -2.0f, 24.0f, 4.0f, 6.0f),
            PivotPose.of(-2.0f, 1.5f, 0.0f));
        return TexturedModelData.of(md, 64, 64);
    }

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Identifier texture,
                       float age, float speed, double wingMotion, boolean flying, int light) {
        float speed01 = MathHelper.clamp(speed / 0.6f, 0f, 1f);
        float flapBase = 0.15f;
        float flapAmp = (float) (0.2 + 0.6 * speed01 + 0.3 * wingMotion / 10.0);
        float flapFreq = (float) (2.0 + 4.0 * speed01 + 2.0 * wingMotion / 10.0);
        float angle = (float) Math.sin(age * flapFreq * 0.35) * flapAmp + (flying ? -0.4f : 0.0f);

        left.yaw = -flapBase - angle;
        right.yaw = flapBase + angle;

        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(texture));
        ms.push();
        root.render(ms, vc, light, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }

    public static void translateToBack(MatrixStack ms, float bodyYaw, float height) {
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        ms.translate(0, height - 1.2f, 0.06f);
    }
}
