package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class KeystrokesFeature {
    private static final int BOX_W = 64;
    private static final int BOX_H = 76;
    private static final int MARGIN = 4;

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "keystrokes"),
                KeystrokesFeature::render);
    }

    /** {x, y, genişlik, yükseklik} (ekran birimi, float) */
    private static float[] place(int guiW, int guiH, float s) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        float w = BOX_W * s;
        float h = BOX_H * s;
        float x;
        float y;
        if (cfg.keysPosX >= 0 && cfg.keysPosY >= 0) {
            x = (float) (cfg.keysPosX * Math.max(0f, guiW - w));
            y = (float) (cfg.keysPosY * Math.max(0f, guiH - h));
        } else {
            x = guiW - w - MARGIN;
            y = guiH - h - MARGIN;
        }
        return new float[]{x, y, w, h};
    }

    /** Editör için: {x, y, genişlik, yükseklik} */
    public static int[] bounds(int guiW, int guiH) {
        float s = (float) Math.max(0.1, LovaConfig.INSTANCE.keysScale);
        float[] p = place(guiW, guiH, s);
        return new int[]{Math.round(p[0]), Math.round(p[1]), Math.round(p[2]), Math.round(p[3])};
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.keysEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float scale = (float) cfg.keysScale;
        if (scale < 0.05f) {
            return;
        }
        int rgb = cfg.keysRgb & 0xFFFFFF;
        float[] p = place(g.guiWidth(), g.guiHeight(), scale);

        g.pose().pushMatrix();
        g.pose().translate(p[0], p[1]);
        g.pose().scale(scale, scale);

        key(g, mc, 22, 0, 20, 20, "W", mc.options.keyUp.isDown(), rgb);
        key(g, mc, 0, 22, 20, 20, "A", mc.options.keyLeft.isDown(), rgb);
        key(g, mc, 22, 22, 20, 20, "S", mc.options.keyDown.isDown(), rgb);
        key(g, mc, 44, 22, 20, 20, "D", mc.options.keyRight.isDown(), rgb);
        key(g, mc, 0, 44, 64, 14, "___", mc.options.keyJump.isDown(), rgb);
        key(g, mc, 0, 60, 31, 16, "LMB", mc.options.keyAttack.isDown(), rgb);
        key(g, mc, 33, 60, 31, 16, "RMB", mc.options.keyUse.isDown(), rgb);

        g.pose().popMatrix();
    }

    private static void key(GuiGraphics g, Minecraft mc, int x, int y, int w, int h,
                            String label, boolean down, int rgb) {
        int bg = down ? (0xB0000000 | rgb) : 0x66000000;
        g.fill(x, y, x + w, y + h, bg);
        int tx = x + (w - mc.font.width(label)) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(mc.font, label, tx, ty, 0xFFFFFFFF, true);
    }
}
