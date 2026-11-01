package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DamageIndicatorFeature {
    private static final int MAX_ACTIVE = 20;
    private static final double RISE_BLOCKS = 0.8;

    private static final class Popup {
        final double x;
        final double y;
        final double z;
        final float damage;
        final long start;

        Popup(double x, double y, double z, float damage, long start) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.damage = damage;
            this.start = start;
        }
    }

    private static final List<Popup> popups = new ArrayList<>();

    private static LivingEntity target = null;
    private static float targetHealth = 0f;
    private static int targetTicks = 0;

    public static void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            Minecraft mc = Minecraft.getInstance();
            if (player == mc.player && entity instanceof LivingEntity le) {
                if (target != le) {
                    target = le;
                    targetHealth = le.getHealth();
                }
                targetTicks = 10;
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "damage_indicator"),
                DamageIndicatorFeature::render);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null) {
            popups.clear();
            target = null;
            return;
        }
        long now = System.currentTimeMillis();

        if (target != null) {
            float cur = target.getHealth();
            if (cur < targetHealth) {
                float dmg = targetHealth - cur;
                LovaConfig cfg = LovaConfig.INSTANCE;
                if (cfg.damageIndicatorEnabled && cfg.coinEnabled) {
                    CoinFeature.spawn(mc, target.getX(), target.getY(), target.getZ(),
                            target.getBbHeight(), formatDamage(dmg), now);
                } else {
                    spawn(target.getX(), target.getY() + target.getBbHeight() + 0.3,
                            target.getZ(), dmg, now);
                }
                targetHealth = cur;
                target = null;
            } else {
                targetHealth = cur;
                if (--targetTicks <= 0) {
                    target = null;
                }
            }
        }

        long life = lifeMs();
        popups.removeIf(p -> now - p.start > life);
    }

    private static void spawn(double x, double y, double z, float dmg, long now) {
        if (!LovaConfig.INSTANCE.damageIndicatorEnabled) {
            return;
        }
        if (popups.size() >= MAX_ACTIVE) {
            popups.remove(0);
        }
        popups.add(new Popup(x, y, z, dmg, now));
    }

    private static long lifeMs() {
        return Math.max(1L, (long) (LovaConfig.INSTANCE.damageIndicatorSec * 1000.0));
    }

    public static String formatDamage(float d) {
        if (Math.abs(d - Math.round(d)) < 0.05f) {
            return "-" + Math.round(d);
        }
        return "-" + String.format(Locale.ROOT, "%.1f", d);
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.damageIndicatorEnabled || popups.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float scale = (float) cfg.damageIndicatorScale;
        if (scale < 0.05f) {
            return;
        }

        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 cp = cam.position();
        Quaternionf inv = new Quaternionf(cam.rotation()).conjugate();
        float tanHalf = (float) Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        int w = g.guiWidth();
        int h = g.guiHeight();
        float aspect = (float) w / (float) h;

        long now = System.currentTimeMillis();
        long life = lifeMs();
        int rgb = cfg.damageIndicatorRgb & 0xFFFFFF;

        for (Popup p : popups) {
            float age = (now - p.start) / (float) life;
            if (age < 0f || age >= 1f) {
                continue;
            }
            int a = (int) (255f * (1f - age));
            if (a < 8) {
                continue;
            }

            Vector3f v = new Vector3f(
                    (float) (p.x - cp.x),
                    (float) (p.y + age * RISE_BLOCKS - cp.y),
                    (float) (p.z - cp.z));
            v.rotate(inv);
            float depth = -v.z;
            if (depth < 0.1f) {
                continue;
            }
            float sx = (v.x / (depth * tanHalf * aspect) * 0.5f + 0.5f) * w;
            float sy = (0.5f - v.y / (depth * tanHalf) * 0.5f) * h;

            String text = formatDamage(p.damage);
            int color = (a << 24) | rgb;

            g.pose().pushMatrix();
            g.pose().translate(sx, sy);
            g.pose().scale(scale, scale);
            g.drawString(mc.font, text, -mc.font.width(text) / 2, -4, color, true);
            g.pose().popMatrix();
        }
    }
}
