package com.ezzclient.mixin;

import com.ezzclient.cosmetics.render.CosmeticFeature;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ezz$addCosmeticFeature(net.minecraft.client.render.entity.EntityRendererFactory.Context ctx,
                                        boolean smallArms, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        java.util.List<FeatureRenderer<?>> features = ((LivingEntityRendererAccessor) (Object) this).ezz$getFeatures();
        features.add(new CosmeticFeature((PlayerEntityRenderer) (Object) this, ctx.getModelLoader()));
    }
}
