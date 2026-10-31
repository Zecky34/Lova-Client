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

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.comboEnabled || combo <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        String text = "x" + combo;
        float scale = (float) cfg.comboScale;
        int color = 0xFF000000 | (cfg.comboRgb & 0xFFFFFF);

        g.pose().pushMatrix();
        g.pose().translate(g.guiWidth() / 2f, g.guiHeight() / 2f + 12f);
        g.pose().scale(scale, scale);
        g.drawString(mc.font, text, -mc.font.width(text) / 2, 0, color, true);
        g.pose().popMatrix();
    }
}
