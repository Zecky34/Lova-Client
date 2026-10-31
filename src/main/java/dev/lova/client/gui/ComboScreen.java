package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ComboScreen extends Screen {
    private final Screen parent;

    public ComboScreen(Screen parent) {
        super(Component.literal("Kombo"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Kombo: " + (value ? "AÇIK" : "KAPALI"));
    }

    private static int withChannel(int rgb, int shift, int value) {
        int v = Math.max(0, Math.min(255, value));
        return (rgb & ~(0xFF << shift)) | (v << shift);
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Kombo ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.comboEnabled), b -> {
            cfg.comboEnabled = !cfg.comboEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.comboEnabled));
        }).bounds(x, y0, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step, w, h, "Sıfırlanma",
                1, 10, cfg.comboResetSec, 1, " sn",
                v -> cfg.comboResetSec = Math.round(v)));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Boyut",
                0, 2, cfg.comboScale, 100, "%",
                v -> cfg.comboScale = v));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 3, w, h, "Kırmızı",
                0, 255, (cfg.comboRgb >> 16) & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 4, w, h, "Yeşil",
                0, 255, (cfg.comboRgb >> 8) & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 5, w, h, "Mavi",
                0, 255, cfg.comboRgb & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 0, (int) Math.round(v))));

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y0 + step * 6 + 8, w, h).build());
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
