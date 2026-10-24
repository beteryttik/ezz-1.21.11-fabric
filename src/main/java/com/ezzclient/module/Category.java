package com.ezzclient.module;

public enum Category {
    ALL("All"), HUD("HUD"), VISUAL("Visual"), UTILITY("Utility"), OTHER("Other");
    public final String label;
    Category(String label) { this.label = label; }
}
