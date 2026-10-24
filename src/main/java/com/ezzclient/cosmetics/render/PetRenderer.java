package com.ezzclient.cosmetics.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Пет — маленькая кукла рядом с плечом.
 * clone = текстура — СКИН ИГРОКА (клон твоего скина).
 * Прочие петы — свои 64x64 текстуры с той же гуманоидной раскладкой.
 */
public class PetRenderer {
    private static ModelPart model = null;
    private static boolean small = false;

    public static void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        var loader = mc.getEntityRenderDispatcher().getModelSet();
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();
        root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-2f, -4f, -2f, 4f, 4f, 4f),
                PivotPose.of(0f, 6f, 0f));
        root.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-2f, 0f, -1f, 4f, 6f, 2f),
                PivotPose.of(0f, 6f, 0f));
        root.addChild("arm", ModelPartBuilder.create().uv(40, 16).cuboid(-1f, 0f, -1f, 1.6f, 6f, 1.6f),
                PivotPose.of(-2.4f, 6f, 0f));
        root.addChild("leg", ModelPartBuilder.create().uv(0, 16).cuboid(-1f, 6f, -1f, 1.6f, 6f, 1.6f),
                PivotPose.of(0.8f, 6f, 0f));
        model = TexturedModelData.of(md, 64, 64).bake();
        small = true;
    }

    public static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || model == null) return;
        String pet = com.ezzclient.cosmetics.CosmeticManager.activePet;
        if (pet == null) return;
        if (!com.ezzclient.cosmetics.CosmeticManager.visibleFor(mc.player.getUuid())) return;

        Identifier tex;
        if (pet.equals("clone")) {
            tex = mc.player.getSkin().texture();
        } else {
            tex = Identifier.of("ezzclient", "textures/cosmetic/pets/" + pet + ".png");
        }

        double scale = com.ezzclient.cosmetics.CosmeticManager.petScale * 0.4;
        long time = mc.player.age;
        float bob = MathHelper.sin(time * 0.12f) * 0.12f;
        float yaw = Math.toRadians(mc.player.getYaw());

        var cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();
        ms.push();
        ms.translate(mc.player.getX() - cam.x, mc.player.getY() - cam.y, mc.player.getZ() - cam.z);
        ms.translate(0.55f, 1.35f + bob, -0.1f);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(float) Math.toDegrees(yaw) + 180.0f));
        ms.scale((float) scale, (float) scale, (float) scale);

        // анимация: голова крутится, лапки качаются
        model.getChild("head").yaw = MathHelper.sin(time * 0.05f) * 0.4f;
        model.getChild("arm").pitch = MathHelper.sin(time * 0.1f) * 0.3f;
        model.getChild("leg").pitch = -MathHelper.sin(time * 0.1f) * 0.3f;

        VertexConsumerProvider vcp = ctx.consumers();
        if (vcp != null) {
            VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(tex));
            model.render(ms, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
        }
        ms.pop();
    }
}
