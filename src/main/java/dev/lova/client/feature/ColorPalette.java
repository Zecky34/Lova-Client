package dev.lova.client.feature;

public class ColorPalette {
    public static final String[] NAMES = {
            "Beyaz", "Kırmızı", "Yeşil", "Mavi", "Sarı", "Mor", "Camgöbeği", "Turuncu"
    };

    public static final int[] RGB = {
            0xFFFFFF, 0xFF3030, 0x30FF30, 0x3060FF, 0xFFFF30, 0xB040FF, 0x30FFFF, 0xFF9020
    };

    public static int get(int index) {
        return RGB[Math.floorMod(index, RGB.length)];
    }

    public static String name(int index) {
        return NAMES[Math.floorMod(index, NAMES.length)];
    }
}
