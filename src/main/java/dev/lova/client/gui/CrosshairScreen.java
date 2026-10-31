package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import dev.lova.client.feature.CrosshairFeature;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CrosshairScreen extends Screen {
    private final Screen parent;

    public CrosshairScreen(Screen parent) {
        super(Component.literal("Crosshair"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component styleLabel(int style) {
        int len = CrosshairFeature.STYLE_NAMES.length;
        return Component.literal("Stil: " + CrosshairFeature.STYLE_NAMES[Math.floorMod(style, len)]);
    }

    private static int withChannel(int rgb, int shift, int value) {
        int v = Math.max(0, Math.min(255, value));
        return (rgb & ~(0xFF << shift)) | (v << shift);
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

        Button header = Button.builder(Component.literal("Crosshair ayarları"), b -> {})
                .bounds(this.width / 2 - 100, 4, 200, 20).build();
        header.active = false;
        this.addRenderableWidget(header);

        // Sol sütun
        this.addRenderableWidget(Button.builder(onOff("Crosshair", cfg.crosshairEnabled), b -> {
            cfg.crosshairEnabled = !cfg.crosshairEnabled;
            LovaConfig.save();
            b.setMessage(onOff("Crosshair", cfg.crosshairEnabled));
        }).bounds(lx, y0, w, h).build());

        this.addRenderableWidget(Button.builder(styleLabel(cfg.crosshairStyle), b -> {
            int len = CrosshairFeature.STYLE_NAMES.length;
            cfg.crosshairStyle = Math.floorMod(cfg.crosshairStyle + 1, len);
            LovaConfig.save();
            b.setMessage(styleLabel(cfg.crosshairStyle));
        }).bounds(lx, y0 + step, w, h).build());

        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 2, w, h, "Boyut",
                1, 16, cfg.crosshairSize, 1, "",
                v -> cfg.crosshairSize = (int) Math.round(v)));

        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 3, w, h, "Saydamlık",
                0, 1, cfg.crosshairOpacity, 100, "%",
                v -> cfg.crosshairOpacity = v));

        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 4, w, h, "Kırmızı",
                0, 255, (cfg.crosshairColor >> 16) & 0xFF, 1, "",
                v -> cfg.crosshairColor = withChannel(cfg.crosshairColor, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 5, w, h, "Yeşil",
                0, 255, (cfg.crosshairColor >> 8) & 0xFF, 1, "",
                v -> cfg.crosshairColor = withChannel(cfg.crosshairColor, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 6, w, h, "Mavi",
                0, 255, cfg.crosshairColor & 0xFF, 1, "",
                v -> cfg.crosshairColor = withChannel(cfg.crosshairColor, 0, (int) Math.round(v))));

        // Sağ sütun
        this.addRenderableWidget(Button.builder(Component.literal("Kendi crosshair'ini çiz"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new CrosshairEditorScreen(this));
        }).bounds(rx, y0, w, h).build());

        this.addRenderableWidget(Button.builder(onOff("Hedefte renk", cfg.crosshairTargetEnabled), b -> {
            cfg.crosshairTargetEnabled = !cfg.crosshairTargetEnabled;
            LovaConfig.save();
            b.setMessage(onOff("Hedefte renk", cfg.crosshairTargetEnabled));
        }).bounds(rx, y0 + step, w, h).build());

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 2, w, h, "Hedef kırmızı",
                0, 255, (cfg.crosshairTargetColor >> 16) & 0xFF, 1, "",
                v -> cfg.crosshairTargetColor = withChannel(cfg.crosshairTargetColor, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 3, w, h, "Hedef yeşil",
                0, 255, (cfg.crosshairTargetColor >> 8) & 0xFF, 1, "",
                v -> cfg.crosshairTargetColor = withChannel(cfg.crosshairTargetColor, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 4, w, h, "Hedef mavi",
                0, 255, cfg.crosshairTargetColor & 0xFF, 1, "",
                v -> cfg.crosshairTargetColor = withChannel(cfg.crosshairTargetColor, 0, (int) Math.round(v))));

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
