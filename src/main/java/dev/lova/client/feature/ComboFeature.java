package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

public class ComboFeature {
    private static int combo = 0;
    private static long lastHitMs = 0;

    private static LivingEntity target = null;
    private static int targetPrevHurt = 0;
    private static int targetTicks = 0;

    private static int lastPlayerHurt = 0;

    public static void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            Minecraft mc = Minecraft.getInstance();
            if (player == mc.player && entity instanceof LivingEntity le) {
                target = le;
                targetPrevHurt = le.hurtTime;
                targetTicks = 8;
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "combo"),
                ComboFeature::render);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || !mc.player.isAlive()) {
            combo = 0;
            target = null;
            lastPlayerHurt = 0;
            return;
        }

        // Hasar yedin: kombo biter
        int ph = mc.player.hurtTime;
        if (ph > lastPlayerHurt) {
            combo = 0;
        }
        lastPlayerHurt = ph;

        // Vurduğumuz hedefin hasar animasyonu başladı mı?
        long now = System.currentTimeMillis();
        if (target != null) {
            int cur = target.hurtTime;
            if (cur > targetPrevHurt) {
                combo++;
                lastHitMs = now;
                target = null;
            } else {
                targetPrevHurt = cur;
                if (--targetTicks <= 0) {
                    target = null;
                }
            }
        }

        // Süre doldu
        if (combo > 0 && now - lastHitMs > LovaConfig.INSTANCE.comboResetSec * 1000.0) {
            combo = 0;
        }
    }

    /** {x, y, genişlik, yükseklik} (ekran birimi, float) */
    private static float[] place(Minecraft mc, int guiW, int guiH, String text, float s) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        float w = mc.font.width(text) * s;
        float h = mc.font.lineHeight * s;
        float x;
        float y;
        if (cfg.comboPosX >= 0 && cfg.comboPosY >= 0) {
            x = (float) (cfg.comboPosX * Math.max(0f, guiW - w));
            y = (float) (cfg.comboPosY * Math.max(0f, guiH - h));
        } else {
            x = guiW / 2f - w / 2f;
            y = guiH / 2f + 12f;
        }
        return new float[]{x, y, w, h};
    }

    private static void drawText(GuiGraphics g, Minecraft mc, String text,
                                 float x, float y, float s, int color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(s, s);
        g.drawString(mc.font, text, 0, 0, color, true);
        g.pose().popMatrix();
    }

    /** Editör için: {x, y, genişlik, yükseklik} */
    public static int[] bounds(Minecraft mc, int guiW, int guiH) {
        String text = combo > 0 ? "x" + combo : "x7";
        float s = (float) Math.max(0.1, LovaConfig.INSTANCE.comboScale);
        float[] p = place(mc, guiW, guiH, text, s);
        return new int[]{Math.round(p[0]), Math.round(p[1]), Math.round(p[2]), Math.round(p[3])};
    }

    /** Editör için: kombo yokken örnek "x7" çizer. */
    public static void drawPreview(GuiGraphics g, Minecraft mc) {
        if (combo > 0) {
            return;
        }
        LovaConfig cfg = LovaConfig.INSTANCE;
        float s = (float) Math.max(0.1, cfg.comboScale);
        float[] p = place(mc, g.guiWidth(), g.guiHeight(), "x7", s);
        drawText(g, mc, "x7", p[0], p[1], s, 0xAA000000 | (cfg.comboRgb & 0xFFFFFF));
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.comboEnabled || combo <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float s = (float) cfg.comboScale;
        if (s < 0.05f) {
            return;
        }
        String text = "x" + combo;
        float[] p = place(mc, g.guiWidth(), g.guiHeight(), text, s);
        drawText(g, mc, text, p[0], p[1], s, 0xFF000000 | (cfg.comboRgb & 0xFFFFFF));
    }
}
