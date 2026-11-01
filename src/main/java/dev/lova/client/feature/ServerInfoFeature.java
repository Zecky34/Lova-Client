package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ServerInfoFeature {

    private static final int PAD_X = 3;
    private static final int PAD_Y = 2;
    private static final int MARGIN = 4;

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "serverinfo"),
                ServerInfoFeature::render);
private static List<String> lines(Minecraft mc, boolean editor) {
    LovaConfig cfg = LovaConfig.INSTANCE;
    List<String> l = new ArrayList<>();
    ServerData sd = mc.getCurrentServer();
    if (sd != null && mc.getConnection() != null) {
        l.add(sd.ip);
        if (cfg.serverInfoPlayers) {
            l.add("Oyuncu: " + (sd.players != null ? sd.players.online() : mc.getConnection().getOnlinePlayers().size()));
        }
    } else if (editor) {
        l.add("play.ornek.com");
        if (cfg.serverInfoPlayers) {
            l.add("Oyuncu: 0");
        }
    }
    return l;
}
        List<String> l = new ArrayList<>();
        ServerData sd = mc.getCurrentServer();
        if (sd != null && mc.getConnection() != null) {
            l.add(sd.ip);
l.add("Oyuncu: " + (sd.players != null ? sd.players.online() : mc.getConnection().getOnlinePlayers().size()));
        } else if (editor) {
            l.add("play.ornek.com");
            l.add("Oyuncu: 0");
        }
        return l;
    }

    /** {x, y, genişlik, yükseklik, içGenişlik, içYükseklik, satırYüksekliği} */
    private static float[] layout(Minecraft mc, List<String> lines, int guiW, int guiH, float s) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int lh = mc.font.lineHeight + 2;
        int maxW = 0;
        for (String t : lines) {
            maxW = Math.max(maxW, mc.font.width(t));
        }
        int totalH = lines.size() * lh - 2;
        float w = (maxW + PAD_X * 2) * s;
        float h = (totalH + PAD_Y * 2) * s;
        float x;
        float y;
        if (cfg.serverInfoPosX >= 0 && cfg.serverInfoPosY >= 0) {
            x = (float) (cfg.serverInfoPosX * Math.max(0f, guiW - w));
            y = (float) (cfg.serverInfoPosY * Math.max(0f, guiH - h));
        } else {
            x = (guiW - w) / 2f;
            y = MARGIN;
        }
        return new float[]{x, y, w, h, maxW, totalH, lh};
    }

    /** Editör için: {x, y, genişlik, yükseklik} */
    public static int[] bounds(Minecraft mc, int guiW, int guiH) {
        float s = (float) Math.max(0.1, LovaConfig.INSTANCE.serverInfoScale);
        float[] p = layout(mc, lines(mc, true), guiW, guiH, s);
        return new int[]{Math.round(p[0]), Math.round(p[1]), Math.round(p[2]), Math.round(p[3])};
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.serverInfoEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float scale = (float) cfg.serverInfoScale;
        if (scale < 0.05f) {
            return;
        }
        List<String> lines = lines(mc, false);
        if (lines.isEmpty()) {
            return;
        }
        float[] p = layout(mc, lines, g.guiWidth(), g.guiHeight(), scale);
        int color = 0xFF000000 | (cfg.serverInfoRgb & 0xFFFFFF);
        int innerW = (int) p[4];
        int totalH = (int) p[5];
        int lh = (int) p[6];
        g.pose().pushMatrix();
        g.pose().translate(p[0], p[1]);
        g.pose().scale(scale, scale);
        g.fill(0, 0, innerW + PAD_X * 2, totalH + PAD_Y * 2, 0x80000000);
        for (int k = 0; k < lines.size(); k++) {
            g.drawString(mc.font, lines.get(k), PAD_X, PAD_Y + k * lh, color, true);
        }
        g.pose().popMatrix();
    }
}
