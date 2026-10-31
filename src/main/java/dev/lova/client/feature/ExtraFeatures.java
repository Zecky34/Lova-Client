package dev.lova.client.feature;

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

public class ExtraFeatures {
    private static int combo = 0;
    private static long lastHitMs = 0;
    private static LivingEntity pending;
    private static int pendingTicks;
    private static int pendingStartHurt;
    private static int prevHurt;

    public static void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            Minecraft mc = Minecraft.getInstance();
            if (player == mc.player && entity instanceof LivingEntity le) {
                pending = le;
                pendingTicks = 8;
                pendingStartHurt = le.hurtTime;
            }
            return InteractionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(ExtraFeatures::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("lovaclient", "overlay"), ExtraFeatures::renderOverlay);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("lovaclient", "extras_hud"), ExtraFeatures::renderHud);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null) {
            combo = 0;
            pending = null;
            return;
        }
        long now = System.currentTimeMillis();
        if (mc.player.hurtTime > prevHurt) {
            combo = 0;
        }
        prevHurt = mc.player.hurtTime;
        if (pending != null) {
            if (pending.hurtTime > pendingStartHurt) {
                combo++;
                lastHitMs = now;
                pending = null;
            } else if (--pendingTicks <= 0) {
                pending = null;
            }
        }
        if (combo > 0 && now - lastHitMs > LovaConfig.INSTANCE.comboResetSec * 1000.0) {
            combo = 0;
        }
    }

    private static void renderOverlay(GuiGraphics g, DeltaTracker dt) {
        LovaConfig c = LovaConfig.INSTANCE;
        int w = g.guiWidth();
        int h = g.guiHeight();
        if (c.filterEnabled) {
            int a = (int) (c.filterStrength * 0.6 * 255.0);
            if (a > 0) {
                g.fill(0, 0, w, h, (a << 24) | (c.filterRgb & 0xFFFFFF));
            }
        }
        if (c.vignetteEnabled) {
            int n = 20;
            float size = Math.min(w, h) * 0.35f;
            for (int i = 0; i < n; i++) {
                float t = 1f - (float) i / n;
                int a = (int) (c.vignetteStrength * 255.0 * t * t);
                if (a <= 0) continue;
                int col = (Math.min(a, 255) << 24) | (c.vignetteRgb & 0xFFFFFF);
                int i0 = (int) (i * size / n);
                int i1 = (int) ((i + 1) * size / n);
                if (i1 <= i0) continue;
                g.fill(i0, i0, w - i0, i1, col);
                g.fill(i0, h - i1, w - i0, h - i0, col);
                g.fill(i0, i1, i1, h - i1, col);
                g.fill(w - i1, i1, w - i0, h - i1, col);
            }
        }
    }

    private static void renderHud(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LovaConfig c = LovaConfig.INSTANCE;
        int w = g.guiWidth();
        int h = g.guiHeight();

        if (c.comboEnabled && combo > 0) {
            g.pose().pushMatrix();
            g.pose().translate(w / 2f, h / 2f + 24f);
            g.pose().scale(2f, 2f);
            g.drawCenteredString(mc.font, "x" + combo, 0, 0, 0xFF000000 | (c.comboRgb & 0xFFFFFF));
            g.pose().popMatrix();
        }

        if (c.keysEnabled) {
            float s = (float) c.keysScale;
            float bw = 66 * s;
            float bh = 76 * s;
            boolean left = c.keysCorner == 0 || c.keysCorner == 2;
            boolean top = c.keysCorner == 0 || c.keysCorner == 1;
            float ox = left ? 4 : w - bw - 4;
            float oy = top ? 4 : h - bh - 4;
            int col = c.keysRgb & 0xFFFFFF;
            g.pose().pushMatrix();
            g.pose().translate(ox, oy);
            g.pose().scale(s, s);
            key(g, mc, 22, 0, 20, 20, "W", mc.options.keyUp.isDown(), col);
            key(g, mc, 0, 22, 20, 20, "A", mc.options.keyLeft.isDown(), col);
            key(g, mc, 22, 22, 20, 20, "S", mc.options.keyDown.isDown(), col);
            key(g, mc, 44, 22, 20, 20, "D", mc.options.keyRight.isDown(), col);
            key(g, mc, 0, 44, 64, 14, "___", mc.options.keyJump.isDown(), col);
            key(g, mc, 0, 60, 31, 16, "LMB", mc.options.keyAttack.isDown(), col);
            key(g, mc, 33, 60, 31, 16, "RMB", mc.options.keyUse.isDown(), col);
            g.pose().popMatrix();
        }
    }

    private static void key(GuiGraphics g, Minecraft mc, int x, int y, int w, int h,
                            String label, boolean down, int col) {
        g.fill(x, y, x + w, y + h, down ? (0xB0000000 | col) : 0x66000000);
        g.drawCenteredString(mc.font, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }
}
