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
import java.util.List;
import java.util.Locale;

public class HudFeature {
    public static final String[] CORNER_NAMES = {
            "Sol üst", "Sağ üst", "Sol alt", "Sağ alt"
    };

    private static int frames = 0;
    private static long lastFpsTime = 0;
    private static int fps = 0;

    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown = false;

    public static void init() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "hud"),
                HudFeature::render);
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
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        List<String> lines = new ArrayList<>();

        if (cfg.hudFps) {
            lines.add("FPS: " + fps);
        }
        if (cfg.hudCps) {
            lines.add("CPS: " + clicks.size());
        }
        if (cfg.hudCoords) {
            lines.add(String.format(Locale.ROOT, "XYZ: %d %d %d",
                    (int) Math.floor(player.getX()),
                    (int) Math.floor(player.getY()),
                    (int) Math.floor(player.getZ())));
        }
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
        if (cfg.hudTotem) {
            int totems = 0;
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Items.TOTEM_OF_UNDYING)) {
                    totems += s.getCount();
                }
            }
            lines.add("Totem: " + totems);
        }
        if (cfg.hudArmor) {
            EquipmentSlot[] slots = {
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            };
            String[] names = {"Kask", "Zırh", "Pantolon", "Bot"};
            for (int i = 0; i < slots.length; i++) {
                ItemStack s = player.getItemBySlot(slots[i]);
                if (!s.isEmpty() && s.getMaxDamage() > 0) {
                    int pct = 100 - (s.getDamageValue() * 100) / s.getMaxDamage();
                    lines.add(names[i] + ": " + pct + "%");
                }
            }
        }
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

        if (lines.isEmpty()) {
            return;
        }

        int lh = mc.font.lineHeight + 2;
        int maxW = 0;
        for (String l : lines) {
            maxW = Math.max(maxW, mc.font.width(l));
        }
        int totalH = lines.size() * lh - 2;
        int pad = 3;
        int margin = 4;

        boolean right = cfg.hudCorner == 1 || cfg.hudCorner == 3;
        boolean bottom = cfg.hudCorner == 2 || cfg.hudCorner == 3;
        int x = right ? g.guiWidth() - margin - pad - maxW : margin + pad;
        int y = bottom ? g.guiHeight() - margin - pad - totalH : margin + pad;

        if (cfg.hudBackground) {
            g.fill(x - pad, y - pad, x + maxW + pad, y + totalH + pad, 0x80000000);
        }

        int color = 0xFF000000 | (cfg.hudColor & 0xFFFFFF);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(mc.font, lines.get(i), x, y + i * lh, color, true);
        }
    }
}
