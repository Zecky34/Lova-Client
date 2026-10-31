package dev.lova.client.feature;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class DamageTint {
    // Vanilla hasar flaşı rengi (ARGB)
    private static final int VANILLA = 0xB2FF0000;

    private static DynamicTexture texture = null;
    private static boolean dirty = true;
    private static int lastColor = VANILLA;

    public static void register(DynamicTexture t) {
        texture = t;
        dirty = true;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private static int tintColor(LovaConfig cfg) {
        double opacity = Math.max(0.0, Math.min(1.0, cfg.damageTintOpacity));
        // Oyunun gölgelendiricisinde alfa ters çalışır: düşük alfa = güçlü renk
        int alpha = (int) Math.round((1.0 - opacity) * 255.0);
        return (alpha << 24) | (cfg.damageTintRgb & 0xFFFFFF);
    }

    private static void tick() {
        if (texture == null) {
            return;
        }
        LovaConfig cfg = LovaConfig.INSTANCE;
        int target = cfg.damageTintEnabled ? tintColor(cfg) : VANILLA;
        if (!dirty && target == lastColor) {
            return;
        }
        NativeImage img = texture.getPixels();
        if (img == null) {
            return;
        }
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++) {
                img.setPixel(x, y, target);
            }
        }
        texture.upload();
        lastColor = target;
        dirty = false;
    }
}
