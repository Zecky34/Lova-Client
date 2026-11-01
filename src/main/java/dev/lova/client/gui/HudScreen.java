package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import dev.lova.client.feature.HudFeature;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HudScreen extends Screen {
    private final Screen parent;

    public HudScreen(Screen parent) {
        super(Component.literal("HUD"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component cornerLabel(int corner) {
        int len = HudFeature.CORNER_NAMES.length;
        return Component.literal("Konum: " + HudFeature.CORNER_NAMES[Math.floorMod(corner, len)]);
    }

    private static int withChannel(int rgb, int shift, int value) {
        int v = Math.max(0, Math.min(255, value));
        return (rgb & ~(0xFF << shift)) | (v << shift);
    }

    private Button toggle(int x, int y, int w, int h, String name,
                          BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(onOff(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            LovaConfig.save();
            b.setMessage(onOff(name, get.getAsBoolean()));
        }).bounds(x, y, w, h).build();
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 150;
        int h = 20;
        int step = 22;
        int lx = this.width / 2 - 155;
        int rx = this.width / 2 + 5;
        int y0 = 28;

        Button header = Button.builder(Component.literal("HUD ayarları"), b -> {})
                .bounds(this.width / 2 - 100, 4, 200, 20).build();
        header.active = false;
        this.addRenderableWidget(header);

        // Sol sütun
        this.addRenderableWidget(toggle(lx, y0, w, h, "HUD",
                () -> cfg.hudEnabled, v -> cfg.hudEnabled = v));
        this.addRenderableWidget(toggle(lx, y0 + step, w, h, "FPS",
                () -> cfg.hudFps, v -> cfg.hudFps = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 2, w, h, "CPS",
                () -> cfg.hudCps, v -> cfg.hudCps = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 3, w, h, "Koordinat",
                () -> cfg.hudCoords, v -> cfg.hudCoords = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 4, w, h, "Ping",
                () -> cfg.hudPing, v -> cfg.hudPing = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 5, w, h, "Totem",
                () -> cfg.hudTotem, v -> cfg.hudTotem = v));

        // Sağ sütun
        this.addRenderableWidget(toggle(rx, y0, w, h, "Zırh",
                () -> cfg.hudArmor, v -> cfg.hudArmor = v));
        this.addRenderableWidget(toggle(rx, y0 + step, w, h, "Efektler",
                () -> cfg.hudEffects, v -> cfg.hudEffects = v));
        this.addRenderableWidget(toggle(rx, y0 + step * 2, w, h, "Arka plan",
                () -> cfg.hudBackground, v -> cfg.hudBackground = v));

        this.addRenderableWidget(Button.builder(cornerLabel(cfg.hudCorner), b -> {
            int len = HudFeature.CORNER_NAMES.length;
            cfg.hudCorner = Math.floorMod(cfg.hudCorner + 1, len);
            cfg.hudPosX = -1;
            cfg.hudPosY = -1;
            LovaConfig.save();
            b.setMessage(cornerLabel(cfg.hudCorner));
        }).bounds(rx, y0 + step * 3, w, h).build());

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 4, w, h, "Kırmızı",
                0, 255, (cfg.hudColor >> 16) & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 5, w, h, "Yeşil",
                0, 255, (cfg.hudColor >> 8) & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 6, w, h, "Mavi",
                0, 255, cfg.hudColor & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 0, (int) Math.round(v))));

        // Geri
        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(this.width / 2 - 75, y0 + step * 7 + 4, 150, h).build());
    }

    @Override
    public void removed() {
        LovaConfig.save();
    }

    @Override
    public void onClose() {
        LovaConfig.save();
        this.minecraft.setScreen(parent);
    }
}
