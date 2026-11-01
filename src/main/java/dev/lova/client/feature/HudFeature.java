package dev.lova.client.feature;

import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HudFeature {
    public static final String[] CORNER_NAMES = {
            "Sol üst", "Sağ üst", "Sol alt", "Sağ alt"
    };

    public static final String[] ELEMENT_NAMES = {
            "FPS", "CPS", "Koordinat", "Ping", "Totem",
            "Kask", "Göğüslük", "Pantolon", "Bot", "Efektler"
    };
    public static final int COUNT = 10;

    private static final String[][] SAMPLES = {
            {"FPS: 60"},
            {"CPS: 0"},
            {"XYZ: 0 64 0"},
            {"Ping: 0 ms"},
            {"Totem: 0"},
            {"Kask: 100%"},
            {"Zırh: 100%"},
            {"Pantolon: 100%"},
            {"Bot: 100%"},
            {"Hız 2 1:30"}
    };

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] ARMOR_LABELS = {"Kask", "Zırh", "Pantolon", "Bot"};

    private static final int PAD_X = 3;
    private static final int PAD_Y = 2;
    private static final int MARGIN = 4;
    private static final int GAP = 1;

    private static int frames = 0;
    private static long lastFpsTime = 0;
    private static int fps = 0;
    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown = false;

    private static final class El {
        List<String> lines = new ArrayList<>();
        boolean included = false;
        boolean active = false;
        int lh;
        int innerW;
        int totalH;
        float s;
        float x;
        float y;
        float w;
        float h;
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "hud"),
                HudFeature::render);
    }

    public static boolean isElementEnabled(int i) {
        LovaConfig c = LovaConfig.INSTANCE;
        if (!c.hudEnabled) {
            return false;
        }
        return switch (i) {
            case 0 -> c.hudFps;
            case 1 -> c.hudCps;
            case 2 -> c.hudCoords;
            case 3 -> c.hudPing;
            case 4 -> c.hudTotem;
            case 5, 6, 7, 8 -> c.hudArmor;
            default -> c.hudEffects;
        };
    }

    private static List<String> buildElement(int i, Minecraft mc, LocalPlayer player, LovaConfig cfg) {
        List<String> lines = new ArrayList<>();
        switch (i) {
            case 0 -> {
                if (cfg.hudFps) {
                    lines.add("FPS: " + fps);
                }
            }
            case 1 -> {
                if (cfg.hudCps) {
                    lines.add("CPS: " + clicks.size());
                }
            }
            case 2 -> {
                if (cfg.hudCoords) {
                    lines.add(String.format(Locale.ROOT, "XYZ: %d %d %d",
                            (int) Math.floor(player.getX()),
                            (int) Math.floor(player.getY()),
                            (int) Math.floor(player.getZ())));
                }
            }
            case 3 -> {
                if (cfg.hudPing) {
                    var conn = mc.getConnection();
                    String ping = "-";
                    if (conn != null) {
                        var info = conn.getPlayerInfo(player.getUUID());
                        if (info != null) {
                            ping = info.getLatency() + " ms";
                        }
                    }
                    lines.add("Ping: " + ping);
                }
            }
            case 4 -> {
                if (cfg.hudTotem) {
                    int totems = 0;
                    Inventory inv = player.getInventory();
                    for (int k = 0; k < inv.getContainerSize(); k++) {
                        ItemStack s = inv.getItem(k);
                        if (s.is(Items.TOTEM_OF_UNDYING)) {
                            totems += s.getCount();
                        }
                    }
                    lines.add("Totem: " + totems);
                }
            }
            case 5, 6, 7, 8 -> {
                if (cfg.hudArmor) {
                    ItemStack s = player.getItemBySlot(ARMOR_SLOTS[i - 5]);
                    if (!s.isEmpty() && s.getMaxDamage() > 0) {
                        int pct = 100 - (s.getDamageValue() * 100) / s.getMaxDamage();
                        lines.add(ARMOR_LABELS[i - 5] + ": " + pct + "%");
                    }
                }
            }
            default -> {
                if (cfg.hudEffects) {
                    for (MobEffectInstance e : player.getActiveEffects()) {
                        String name = e.getEffect().value().getDisplayName().getString();
                        int lvl = e.getAmplifier() + 1;
                        String time;
                        if (e.isInfiniteDuration()) {
                            time = "**";
                        } else {
                            int sec = e.getDuration() / 20;
                            time = String.format(Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
                        }
                        lines.add(name + (lvl > 1 ? " " + lvl : "") + " " + time);
                    }
                }
            }
        }
        return lines;
    }

    private static float stackHeight(List<El> list) {
        float sum = 0;
        for (El e : list) {
            sum += e.h;
        }
        if (list.size() > 1) {
            sum += GAP * (list.size() - 1);
        }
        return sum;
    }

    private static void placeStack(List<El> list, float startY, boolean right, int guiW) {
        float cur = startY;
        for (El e : list) {
            e.y = cur;
            e.x = right ? guiW - MARGIN - e.w : MARGIN;
            cur += e.h + GAP;
        }
    }

    private static El[] computeAll(Minecraft mc, int guiW, int guiH, boolean editor) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        LocalPlayer player = mc.player;
        boolean master = cfg.hudEnabled && player != null;
        int lh = mc.font.lineHeight + 2;

        El[] els = new El[COUNT];
        List<El> stackA = new ArrayList<>();
        List<El> stackB = new ArrayList<>();

        for (int i = 0; i < COUNT; i++) {
            El e = new El();
            els[i] = e;
            if (master) {
                e.lines = buildElement(i, mc, player, cfg);
            }
            e.active = !e.lines.isEmpty();
            if (!e.active) {
                if (!editor) {
                    continue;
                }
                e.lines = new ArrayList<>(Arrays.asList(SAMPLES[i]));
            }
            e.included = true;
            e.lh = lh;
            int maxW = 0;
            for (String s : e.lines) {
                maxW = Math.max(maxW, mc.font.width(s));
            }
            e.innerW = maxW;
            e.totalH = e.lines.size() * lh - 2;
            e.s = (float) Math.max(0.1, cfg.hudPartScale[i]);
            e.w = (maxW + PAD_X * 2) * e.s;
            e.h = (e.totalH + PAD_Y * 2) * e.s;

            double px = cfg.hudPartPosX[i];
            double py = cfg.hudPartPosY[i];
            if (px >= 0 && py >= 0) {
                e.x = (float) (px * Math.max(0f, guiW - e.w));
                e.y = (float) (py * Math.max(0f, guiH - e.h));
            } else if (e.active) {
                stackA.add(e);
            } else {
                stackB.add(e);
            }
        }

        boolean right = cfg.hudCorner == 1 || cfg.hudCorner == 3;
        boolean bottom = cfg.hudCorner == 2 || cfg.hudCorner == 3;

        float ha = stackHeight(stackA);
        float startA = bottom ? guiH - MARGIN - ha : MARGIN;
        placeStack(stackA, startA, right, guiW);

        if (!stackB.isEmpty()) {
            float hb = stackHeight(stackB);
            float startB;
            if (bottom) {
                startB = (stackA.isEmpty() ? guiH - MARGIN : startA - GAP) - hb;
            } else {
                startB = stackA.isEmpty() ? MARGIN : MARGIN + ha + GAP;
            }
            placeStack(stackB, startB, right, guiW);
        }
        return els;
    }

    /** Editör için: her eleman {x, y, genişlik, yükseklik} */
    public static int[][] boundsAll(Minecraft mc, int guiW, int guiH) {
        El[] els = computeAll(mc, guiW, guiH, true);
        int[][] r = new int[COUNT][];
        for (int i = 0; i < COUNT; i++) {
            El e = els[i];
            r[i] = new int[]{Math.round(e.x), Math.round(e.y), Math.round(e.w), Math.round(e.h)};
        }
        return r;
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();

        frames++;
        if (now - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }

        boolean down = mc.options.keyAttack.isDown();
        if (down && !wasDown) {
            clicks.addLast(now);
        }
        wasDown = down;
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
            clicks.removeFirst();
        }

        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.hudEnabled) {
            return;
        }
        if (mc.player == null) {
            return;
        }

        El[] els = computeAll(mc, g.guiWidth(), g.guiHeight(), false);
        int color = 0xFF000000 | (cfg.hudColor & 0xFFFFFF);

        for (El e : els) {
            if (!e.included || !e.active) {
                continue;
            }
            g.pose().pushMatrix();
            g.pose().translate(e.x, e.y);
            g.pose().scale(e.s, e.s);
            if (cfg.hudBackground) {
                g.fill(0, 0, e.innerW + PAD_X * 2, e.totalH + PAD_Y * 2, 0x80000000);
            }
            for (int k = 0; k < e.lines.size(); k++) {
                g.drawString(mc.font, e.lines.get(k), PAD_X, PAD_Y + k * e.lh, color, true);
            }
            g.pose().popMatrix();
        }
    }
}
