package dev.lova.client.feature;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Random;

public class ScreenEffectsFeature {

    private static final int TEX = 512;
    private static final int REGION = 384;
    private static final float MAX_ALPHA = 110f;
    private static final Identifier TEX_ID =
            Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "grain");
    private static final Random FRAME_RNG = new Random();

    private static DynamicTexture texture;
    private static double builtStrength = -1;

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "grain"),
                ScreenEffectsFeature::render);
    }

    private static void build(Minecraft mc, double strength) {
        if (texture == null) {
            texture = new DynamicTexture(() -> "lovaclient_grain", TEX, TEX, false);
            mc.getTextureManager().register(TEX_ID, texture);
        }
        NativeImage img = texture.getPixels();
        if (img == null) {
            return;
        }
        Random r = new Random(12345L);
        for (int y = 0; y < TEX; y++) {
            for (int x = 0; x < TEX; x++) {
                int lum = r.nextBoolean() ? 255 : 0;
                int a = (int) (r.nextFloat() * MAX_ALPHA * strength);
                img.setPixel(x, y, (a << 24) | (lum << 16) | (lum << 8) | lum);
            }
        }
        texture.upload();
        builtStrength = strength;
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.grainEnabled) {
            return;
        }
        double s = Math.max(0.0, Math.min(1.0, cfg.grainStrength));
        if (s < 0.01) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (texture == null || Math.abs(builtStrength - s) >= 0.01) {
            build(mc, s);
        }
        if (texture == null) {
            return;
        }
        int gw = g.guiWidth();
        int gh = g.guiHeight();
        int rw = Math.min(gw, REGION);
        int rh = Math.min(gh, REGION);
        int u = 0;
        int v = 0;
        if (cfg.grainMode == 0) {
            u = FRAME_RNG.nextInt(TEX - rw + 1);
            v = FRAME_RNG.nextInt(TEX - rh + 1);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEX_ID, 0, 0, u, v, gw, gh, rw, rh, TEX, TEX);
    }
}
