package com.rrtech.myhabits.data.model;

public class ColorItem {
    private final int color;
    private final boolean isPro;

    public ColorItem(int color, boolean isPro) {
        this.color = color;
        this.isPro = isPro;
    }

    public int getColor() {
        return color;
    }

    public boolean isPro() {
        return isPro;
    }
}
