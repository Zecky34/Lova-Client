package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class KeystrokesScreen extends Screen {
    private final Screen parent;

    public KeystrokesScreen(Screen parent) {
        super(Component.literal("Tuş gösterici"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Tuş gösterici: " + (value ? "AÇIK" : "KAPALI"));
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

        Button header = Button.builder(Component.literal("Tuş gösterici ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.keysEnabled), b -> {
            cfg.keysEnabled = !cfg.keysEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.keysEnabled));
        }).bounds(x, y0, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step, w, h, "Boyut",
                0, 2, cfg.keysScale, 100, "%",
                v -> cfg.keysScale = v));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Kırmızı",
                0, 255, (cfg.keysRgb >> 16) & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 3, w, h, "Yeşil",
                0, 255, (cfg.keysRgb >> 8) & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 4, w, h, "Mavi",
                0, 255, cfg.keysRgb & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 0, (int) Math.round(v))));

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y0 + step * 5 + 8, w, h).build());
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
