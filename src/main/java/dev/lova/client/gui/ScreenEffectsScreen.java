package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScreenEffectsScreen extends Screen {

    private final Screen parent;

    public ScreenEffectsScreen(Screen parent) {
        super(Component.literal("Ekran efektleri"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Tanecik: " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component modeLabel(int mode) {
        return Component.literal("Mod: " + (mode == 0 ? "Hareketli" : "Sabit"));
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Ekran efektleri"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.grainEnabled), b -> {
            cfg.grainEnabled = !cfg.grainEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.grainEnabled));
        }).bounds(x, y0, w, h).build());

        this.addRenderableWidget(Button.builder(modeLabel(cfg.grainMode), b -> {
            cfg.grainMode = cfg.grainMode == 0 ? 1 : 0;
            LovaConfig.save();
            b.setMessage(modeLabel(cfg.grainMode));
        }).bounds(x, y0 + step, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Tanecik",
                0, 1, cfg.grainStrength, 100, "%",
                v -> cfg.grainStrength = v));

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y0 + step * 3 + 8, w, h).build());
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
