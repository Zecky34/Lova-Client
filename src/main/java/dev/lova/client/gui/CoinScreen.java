package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import dev.lova.client.feature.CoinFeature;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CoinScreen extends Screen {
    private final Screen parent;

    public CoinScreen(Screen parent) {
        super(Component.literal("Jeton modu"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Jeton modu: " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component themeLabel(int theme) {
        int len = CoinFeature.THEME_NAMES.length;
        return Component.literal("Renk teması: " + CoinFeature.THEME_NAMES[Math.floorMod(theme, len)]);
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Jeton modu ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.coinEnabled), b -> {
            cfg.coinEnabled = !cfg.coinEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.coinEnabled));
        }).bounds(x, y0, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step, w, h, "Boyut",
                0, 2, cfg.coinScale, 100, "%",
                v -> cfg.coinScale = v));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Yerde kalma",
                0.5, 5, cfg.coinGroundSec, 1000, " ms",
                v -> cfg.coinGroundSec = v));

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 3, w, h, "Düşme hızı",
                0.5, 2, cfg.coinFall, 100, "%",
                v -> cfg.coinFall = v));

        this.addRenderableWidget(Button.builder(themeLabel(cfg.coinTheme), b -> {
            int len = CoinFeature.THEME_NAMES.length;
            cfg.coinTheme = Math.floorMod(cfg.coinTheme + 1, len);
            LovaConfig.save();
            b.setMessage(themeLabel(cfg.coinTheme));
        }).bounds(x, y0 + step * 4, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 5, w, h, "En fazla jeton",
                1, 20, cfg.coinMax, 1, "",
                v -> cfg.coinMax = (int) Math.round(v)));

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
