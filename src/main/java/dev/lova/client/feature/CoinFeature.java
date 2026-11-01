package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoinFeature {
    public static final String[] THEME_NAMES = {"Altın", "Elmas", "Zümrüt", "Ametist", "Kızıl"};

    // her tema: {kenar, gövde, iç halka}
    private static final int[][] THEMES = {
            {0x8A5A00, 0xE6B422, 0xFFE680},
            {0x0B4F6C, 0x33C4E0, 0xB8F2FF},
            {0x0B5D2A, 0x2ECC71, 0xB5F5CF},
            {0x4B1F6F, 0x9B59D6, 0xE0C6F5},
            {0x7A0F0F, 0xE03C3C, 0xFFB3B3}
    };

    private static final double BASE_GRAVITY = 14.0;
    private static final double LAUNCH_VY = 3.5;
    private static final double FADE_SEC = 0.4;
    private static final float TEXT_SCALE = 0.7f;
    private static final Random RNG = new Random();

    private static final class Coin {
        final double x0;
        final double y0;
        final double z0;
        final double vx;
        final double vz;
        final double g;
        final double groundY;
        final double tLand;
        final double groundSec;
        final long start;
        final String text;
        final int theme;

        Coin(double x0, double y0, double z0, double vx, double vz, double g,
             double groundY, double tLand, double groundSec, long start,
             String text, int theme) {
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.vx = vx;
            this.vz = vz;
            this.g = g;
            this.groundY = groundY;
            this.tLand = tLand;
            this.groundSec = groundSec;
            this.start = start;
            this.text = text;
            this.theme = theme;
        }

        double endSec() {
            return tLand + groundSec + FADE_SEC;
        }
    }

    private static final List<Coin> coins = new ArrayList<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "coins"),
                CoinFeature::render);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null) {
            coins.clear();
            return;
        }
        long now = System.currentTimeMillis();
        coins.removeIf(c -> (now - c.start) / 1000.0 >= c.endSec());
    }

    public static void spawn(Minecraft mc, double x, double feetY, double z,
                             double height, String text, long now) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        Camera cam = mc.gameRenderer.getMainCamera();

        Vector3f right = new Vector3f(1f, 0f, 0f).rotate(cam.rotation());
        double rx = right.x;
        double rz = right.z;
        double len = Math.sqrt(rx * rx + rz * rz);
        if (len < 1e-4) {
            rx = 1;
            rz = 0;
        } else {
            rx /= len;
            rz /= len;
        }
        double side = RNG.nextBoolean() ? 1 : -1;
        double speed = 1.6 + RNG.nextDouble() * 0.8;

        double g = BASE_GRAVITY * Math.max(0.1, cfg.coinFall);
        double y0 = feetY + height;
        double groundY = feetY + 0.1;
        double disc = LAUNCH_VY * LAUNCH_VY + 2.0 * g * Math.max(0.0, y0 - groundY);
        double tLand = (LAUNCH_VY + Math.sqrt(disc)) / g;

        int theme = Math.max(0, Math.min(THEMES.length - 1, cfg.coinTheme));

        coins.add(new Coin(x, y0, z, rx * speed * side, rz * speed * side, g,
                groundY, tLand, Math.max(0.1, cfg.coinGroundSec), now, text, theme));
        while (coins.size() > Math.max(1, cfg.coinMax)) {
            coins.remove(0);
        }
    }

    private static float[] project(Camera cam, Quaternionf inv, float tanHalf, float aspect,
                                   int w, int h, double wx, double wy, double wz) {
        Vec3 cp = cam.position();
        Vector3f v = new Vector3f((float) (wx - cp.x), (float) (wy - cp.y), (float) (wz - cp.z));
        v.rotate(inv);
        float depth = -v.z;
        if (depth < 0.1f) {
            return null;
        }
        float sx = (v.x / (depth * tanHalf * aspect) * 0.5f + 0.5f) * w;
        float sy = (0.5f - v.y / (depth * tanHalf) * 0.5f) * h;
        return new float[]{sx, sy};
    }

    private static void band(GuiGraphics g, int ay, int dy, int d0, int d1, int color) {
        int hi = d1 - ay;
        if (hi < 0) {
            return;
        }
        int lo = Math.max(0, d0 - ay);
        if (lo == 0) {
            g.fill(-hi, dy, hi + 1, dy + 1, color);
        } else {
            g.fill(-hi, dy, -lo + 1, dy + 1, color);
            g.fill(lo, dy, hi + 1, dy + 1, color);
        }
    }

    private static void drawCoin(GuiGraphics g, Minecraft mc, float sx, float sy,
                                 String text, int theme, int a, float scale) {
        int hx = (int) Math.ceil(mc.font.width(text) * TEXT_SCALE / 2f);
        int hy = (int) Math.ceil(mc.font.lineHeight * TEXT_SCALE / 2f);
        int inner = Math.max(8, hx + hy + 1);
        int r = inner + 5;

        int[] th = THEMES[theme];
        int rim = (a << 24) | th[0];
        int body = (a << 24) | th[1];
        int ring = (a << 24) | th[2];

        g.pose().pushMatrix();
        g.pose().translate(sx, sy);
        g.pose().scale(scale, scale);

        for (int dy = -r; dy <= r; dy++) {
            int ay = Math.abs(dy);
            band(g, ay, dy, r - 1, r, rim);
            band(g, ay, dy, r - 3, r - 2, body);
            band(g, ay, dy, r - 4, r - 4, ring);
            band(g, ay, dy, 0, r - 5, body);
        }

        g.pose().pushMatrix();
        g.pose().scale(TEXT_SCALE, TEXT_SCALE);
        g.drawString(mc.font, text, -mc.font.width(text) / 2, -mc.font.lineHeight / 2,
                (a << 24) | 0xFFFFFF, true);
        g.pose().popMatrix();

        g.pose().popMatrix();
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.coinEnabled || coins.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float scale = (float) cfg.coinScale;
        if (scale < 0.05f) {
            return;
        }

        Camera cam = mc.gameRenderer.getMainCamera();
        Quaternionf inv = new Quaternionf(cam.rotation()).conjugate();
        float tanHalf = (float) Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        int w = g.guiWidth();
        int h = g.guiHeight();
        float aspect = (float) w / (float) h;
        long now = System.currentTimeMillis();

        for (Coin c : coins) {
            double t = (now - c.start) / 1000.0;
            if (t < 0 || t >= c.endSec()) {
                continue;
            }

            int a = 255;
            double fadeStart = c.tLand + c.groundSec;
            if (t > fadeStart) {
                a = (int) (255.0 * (1.0 - (t - fadeStart) / FADE_SEC));
            }
            if (a < 8) {
                continue;
            }

            double px;
            double py;
            double pz;
            if (t < c.tLand) {
                px = c.x0 + c.vx * t;
                py = c.y0 + LAUNCH_VY * t - 0.5 * c.g * t * t;
                pz = c.z0 + c.vz * t;
            } else {
                px = c.x0 + c.vx * c.tLand;
                py = c.groundY;
                pz = c.z0 + c.vz * c.tLand;
            }

            float[] p = project(cam, inv, tanHalf, aspect, w, h, px, py, pz);
            if (p == null) {
                continue;
            }
            drawCoin(g, mc, p[0], p[1], c.text, c.theme, Math.min(a, 255), scale);
        }
    }
}
