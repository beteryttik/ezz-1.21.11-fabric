package com.ezzclient.hud;

public class HudElement {
    public final String id;
    public int x, y, w = 60, h = 14;
    public final int defaultX, defaultY;
    public HudElement(String id, int defaultX, int defaultY) {
        this.id = id; this.defaultX = defaultX; this.defaultY = defaultY;
        this.x = defaultX; this.y = defaultY;
    }
}
