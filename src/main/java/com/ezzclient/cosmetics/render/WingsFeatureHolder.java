package com.ezzclient.cosmetics.render;

import net.minecraft.client.MinecraftClient;

/** Ленивое создание модели крыльев. */
public class WingsFeatureHolder {
    private static WingsModel model;

    public static WingsModel get() {
        if (model == null) {
            try {
                var loader = MinecraftClient.getInstance().getEntityModelLoader();
                model = new WingsModel(WingsModel.build().bake());
            } catch (Throwable t) {
                return null;
            }
        }
        return model;
    }
}
