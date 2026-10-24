package com.ezzclient.cosmetics;

public class Cosmetic {
    public enum Type { CAPE, WINGS, HAT, PET, AURA }
    public final String id, name, desc;
    public final Type type;
    public Cosmetic(String id, String name, String desc, Type type) {
        this.id = id; this.name = name; this.desc = desc; this.type = type;
    }
    public String texturePath() {
        return "ezzclient:textures/cosmetic/" + type.name().toLowerCase() + "s/" + id + ".png";
    }
}
