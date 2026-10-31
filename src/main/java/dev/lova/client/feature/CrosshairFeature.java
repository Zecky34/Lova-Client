package dev.lova.client.feature;

import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CrosshairFeature {
    public static final String[] STYLE_NAMES = {
            "Artı", "Bitişik artı", "Nokta", "Kare", "Özel"
    };

    public static void init() {
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, delta) -> {
            if (LovaConfig.INSTANCE.crosshairEnabled) {
                draw(graphics);
            } else {
                original.render(graphics, delta);
            }
        });
    }

    private static boolean targeting(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return false;
        }
        return hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity;
    }

    private static void draw(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        LovaConfig cfg = LovaConfig.INSTANCE;
        int rgb = cfg.crosshairColor;
        if (cfg.crosshairTargetEnabled && targeting(mc)) {
            rgb = cfg.crosshairTargetColor;
        }

        double opacity = Math.max(0.0, Math.min(1.0, cfg.crosshairOpacity));
        int alpha = (int) Math.round(opacity * 255.0);
        if (alpha <= 0) {
            return;
        }
        int c = (alpha << 24) | (rgb & 0xFFFFFF);

        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        int s = Math.max(1, cfg.crosshairSize);

        switch (cfg.crosshairStyle) {
            case 0:
                plus(g, cx, cy, s, 2, c);
                break;
            case 1:
                plus(g, cx, cy, s, 0, c);
                break;
            case 2: {
                int half = Math.max(1, s / 4);
                g.fill(cx - half, cy - half, cx + half + 1, cy + half + 1, c);
                break;
            }
            case 3: {
                int h = Math.max(2, s);
                g.fill(cx - h, cy - h, cx + h + 1, cy - h + 1, c);
                g.fill(cx - h, cy + h, cx + h + 1, cy + h + 1, c);
                g.fill(cx - h, cy - h, cx - h + 1, cy + h + 1, c);
                g.fill(cx + h, cy - h, cx + h + 1, cy + h + 1, c);
                break;
            }
            default:
                custom(g, cx, cy, cfg.crosshairCustomPixels, c);
                break;
        }
    }

    private static void plus(GuiGraphics g, int cx, int cy, int s, int gap, int c) {
        g.fill(cx - gap - s, cy, cx - gap, cy + 1, c);
        g.fill(cx + gap + 1, cy, cx + gap + 1 + s, cy + 1, c);
        g.fill(cx, cy - gap - s, cx + 1, cy - gap, c);
        g.fill(cx, cy + gap + 1, cx + 1, cy + gap + 1 + s, c);
        if (gap == 0) {
            g.fill(cx, cy, cx + 1, cy + 1, c);
        }
    }

    private static void custom(GuiGraphics g, int cx, int cy, String pixels, int c) {
        int grid = LovaConfig.GRID;
        if (pixels == null || pixels.length() != grid * grid) {
            return;
        }
        int half = grid / 2;
        for (int y = 0; y < grid; y++) {
            for (int x = 0; x < grid; x++) {
                if (pixels.charAt(y * grid + x) == '1') {
                    int px = cx - half + x;
                    int py = cy - half + y;
                    g.fill(px, py, px + 1, py + 1, c);
                }
            }
        }
    }
}
