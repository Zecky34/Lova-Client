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
        float ox = g.guiWidth() - BOX_W * scale - MARGIN;
        float oy = g.guiHeight() - BOX_H * scale - MARGIN;

        g.pose().pushMatrix();
        g.pose().translate(ox, oy);
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
