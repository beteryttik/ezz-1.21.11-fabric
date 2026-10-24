package com.ezzclient.cosmetics;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CosmeticManager {
    public static final List<Cosmetic> CATALOG = new ArrayList<>();

    // активная косметика
    public static String activeCape = null, activeWings = null, activeHat = null, activePet = null, activeAura = null;
    public static boolean hideElytra = true;
    public static double wingMotion = 5.0;
    public static boolean petIsSkinClone = false;
    public static double petScale = 0.7;
    public static boolean showAura = true;

    private static void add(String id, String name, String desc, Cosmetic.Type t) {
        CATALOG.add(new Cosmetic(id, name, desc, t));
    }

    public static void init() {
        // капы
        add("ezz", "Ezz Cape", "The signature cape of Ezz Client. Purple, proud, pixel-perfect.", Cosmetic.Type.CAPE);
        add("hero", "Hero Cape", "Teal cape for players who save the village at 3 AM.", Cosmetic.Type.CAPE);
        add("anniv", "15th Anniversary", "Creeper green with a festive look.", Cosmetic.Type.CAPE);
        add("reactive", "Reactive Cape", "Glows red when things go wrong.", Cosmetic.Type.CAPE);
        add("fire", "Fire Cape", "Lava-tested, flame-approved.", Cosmetic.Type.CAPE);
        add("ice", "Ice Cape", "Cold to the touch, cool to the eye.", Cosmetic.Type.CAPE);
        add("galactic", "Galactic Cape", "Deep space vibes.", Cosmetic.Type.CAPE);
        add("rainbow", "Rainbow Cape", "All colors at once.", Cosmetic.Type.CAPE);
        // крылья
        add("dragon", "Dragon Wings", "Echo of the last dragon. Spectacular flights above the world.", Cosmetic.Type.WINGS);
        add("angel", "Angel Wings", "Feathered and blessed.", Cosmetic.Type.WINGS);
        add("fluffy", "Fluffy Wings", "Softest wings in the end.", Cosmetic.Type.WINGS);
        add("fairy", "Fairy Wings", "Small, translucent, magical.", Cosmetic.Type.WINGS);
        add("phoenix", "Phoenix Wings", "Reborn with fire particles.", Cosmetic.Type.WINGS);
        add("bat", "Bat Wings", "Fast and silent.", Cosmetic.Type.WINGS);
        // шляпы
        add("crown", "Crown", "For server royalty.", Cosmetic.Type.HAT);
        add("halo", "Halo", "Glowing ring of the chosen.", Cosmetic.Type.HAT);
        add("witch", "Witch Hat", "Cackling included.", Cosmetic.Type.HAT);
        add("tophat", "Top Hat", "Fancy. Very fancy.", Cosmetic.Type.HAT);
        add("santa", "Santa Hat", "Ho ho ho.", Cosmetic.Type.HAT);
        add("horns", "Demon Horns", "Slightly evil.", Cosmetic.Type.HAT);
        add("flower", "Flower Crown", "Peaceful vibes.", Cosmetic.Type.HAT);
        add("wizard", "Wizard Hat", "+10 to spellcasting.", Cosmetic.Type.HAT);
        // петы
        add("bee", "Bee", "Bzzz. Flies near your shoulder.", Cosmetic.Type.PET);
        add("slime", "Slime", "Bouncy little friend.", Cosmetic.Type.PET);
        add("cat", "Cat", "Meow.", Cosmetic.Type.PET);
        add("ghost", "Ghost", "Boo. Transparent buddy.", Cosmetic.Type.PET);
        add("dragon", "Baby Dragon", "Sits on your shoulder.", Cosmetic.Type.PET);
        add("clone", "Skin Clone", "A tiny copy of YOUR skin flying by your shoulder.", Cosmetic.Type.PET);

        loadSaved();
    }

    private static void loadSaved() {
        // подгружается через ConfigManager.loadProfile -> loadJson
    }

    public static Cosmetic byId(String id) {
        for (Cosmetic c : CATALOG) if (c.id.equals(id)) return c;
        return null;
    }

    public static void equip(Cosmetic c) {
        switch (c.type) {
            case CAPE -> activeCape = c.id;
            case WINGS -> activeWings = c.id;
            case HAT -> activeHat = c.id;
            case PET -> { activePet = c.id; petIsSkinClone = c.id.equals("clone"); }
            case AURA -> activeAura = c.id;
        }
        com.ezzclient.config.ConfigManager.save();
    }

    public static void unequip(Cosmetic c) {
        switch (c.type) {
            case CAPE -> { if (activeCape != null && activeCape.equals(c.id)) activeCape = null; }
            case WINGS -> { if (activeWings != null && activeWings.equals(c.id)) activeWings = null; }
            case HAT -> { if (activeHat != null && activeHat.equals(c.id)) activeHat = null; }
            case PET -> { if (activePet != null && activePet.equals(c.id)) activePet = null; }
            case AURA -> { if (activeAura != null && activeAura.equals(c.id)) activeAura = null; }
        }
        com.ezzclient.config.ConfigManager.save();
    }

    public static boolean isActive(Cosmetic c) {
        return switch (c.type) {
            case CAPE -> c.id.equals(activeCape);
            case WINGS -> c.id.equals(activeWings);
            case HAT -> c.id.equals(activeHat);
            case PET -> c.id.equals(activePet);
            case AURA -> c.id.equals(activeAura);
        };
    }

    /** Только локальный игрок? Крылья/капы/шляпы рисуем исключительно для себя. */
    public static boolean visibleFor(UUID uuid) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player != null && uuid.equals(mc.player.getUuid());
    }

    public static void tickParticles(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || activeAura == null || !showAura) return;
        if (mc.player.age % 4 != 0) return;
        var type = switch (activeAura) {
            case "fire" -> ParticleTypes.FLAME;
            case "snow" -> ParticleTypes.SNOWFLAKE;
            case "hearts" -> ParticleTypes.HEART;
            case "ender" -> ParticleTypes.PORTAL;
            default -> ParticleTypes.ELECTRIC_SPARK;
        };
        for (int i = 0; i < 3; i++)
            mc.world.addParticle(type,
                    mc.player.getX() + (Math.random() - 0.5) * 0.8,
                    mc.player.getY() + Math.random() * 1.8,
                    mc.player.getZ() + (Math.random() - 0.5) * 0.8,
                    0, 0.01, 0);
    }

    // ---- сохранение ----
    public static JsonObject saveJson() {
        JsonObject o = new JsonObject();
        if (activeCape != null) o.addProperty("cape", activeCape);
        if (activeWings != null) o.addProperty("wings", activeWings);
        if (activeHat != null) o.addProperty("hat", activeHat);
        if (activePet != null) o.addProperty("pet", activePet);
        if (activeAura != null) o.addProperty("aura", activeAura);
        o.addProperty("hideElytra", hideElytra);
        o.addProperty("wingMotion", wingMotion);
        o.addProperty("petScale", petScale);
        o.addProperty("petIsSkinClone", petIsSkinClone);
        return o;
    }

    public static void loadJson(JsonObject o) {
        if (o.has("cape")) activeCape = o.get("cape").getAsString();
        if (o.has("wings")) activeWings = o.get("wings").getAsString();
        if (o.has("hat")) activeHat = o.get("hat").getAsString();
        if (o.has("pet")) { activePet = o.get("pet").getAsString(); petIsSkinClone = "clone".equals(activePet); }
        if (o.has("aura")) activeAura = o.get("aura").getAsString();
        if (o.has("hideElytra")) hideElytra = o.get("hideElytra").getAsBoolean();
        if (o.has("wingMotion")) wingMotion = o.get("wingMotion").getAsDouble();
        if (o.has("petScale")) petScale = o.get("petScale").getAsDouble();
    }
}
