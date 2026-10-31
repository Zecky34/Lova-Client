package dev.lova.client.gui;

import dev.lova.client.ExtrasConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ExtrasScreen extends Screen {
    private final Screen parent;
    private int page = 0;
    private int x0, cw;
    private static final String[] CORNERS = {"Sol üst", "Sağ üst", "Sol alt", "Sağ alt"};

    public ExtrasScreen(Screen parent) {
        super(Component.literal("Ekstralar"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ExtrasConfig c = ExtrasConfig.get();
        cw = Math.min(220, width - 20);
        x0 = (width - cw) / 2;

        String[] tabs = {"Kombo", "Tuş", "Filtre", "Vinyet", "Geri"};
        int tw = (width - 10) / 5;
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(tabs[i]), b -> {
                if (idx == 4) {
                    onClose();
                } else {
                    page = idx;
                    rebuildWidgets();
                }
            }).bounds(5 + i * tw, 20, tw - 2, 20).build());
        }

        int y = 50;
        switch (page) {
            case 0 -> {
                toggle(y, "Kombo", () -> c.comboEnabled, v -> c.comboEnabled = v);
                slider(x0, y + 24, cw, "Sıfırlanma sn", 1, 10, c.comboResetSec, false, v -> c.comboResetSec = (float) v);
                slider(x0, y + 48, cw, "Boyut", 0.5, 4, c.comboScale, false, v -> c.comboScale = (float) v);
                colorRow(y + 72, c.comboR, c.comboG, c.comboB, v -> c.comboR = (int) v, v -> c.comboG = (int) v, v -> c.comboB = (int) v);
            }
            case 1 -> {
                toggle(y, "Tuş gösterici", () -> c.keysEnabled, v -> c.keysEnabled = v);
                slider(x0, y + 24, cw, "Boyut", 0.5, 2.5, c.keysScale, false, v -> c.keysScale = (float) v);
                addRenderableWidget(Button.builder(Component.literal("Köşe: " + CORNERS[c.keysCorner]), b -> {
                    c.keysCorner = (c.keysCorner + 1) % 4;
                    b.setMessage(Component.literal("Köşe: " + CORNERS[c.keysCorner]));
                }).bounds(x0, y + 48, cw, 20).build());
                colorRow(y + 72, c.keysR, c.keysG, c.keysB, v -> c.keysR = (int) v, v -> c.keysG = (int) v, v -> c.keysB = (int) v);
            }
            case 2 -> {
                toggle(y, "Ekran filtresi", () -> c.filterEnabled, v -> c.filterEnabled = v);
                slider(x0, y + 24, cw, "Güç %", 0, 100, c.filterStrength, true, v -> c.filterStrength = (float) v);
                colorRow(y + 48, c.filterR, c.filterG, c.filterB, v -> c.filterR = (int) v, v -> c.filterG = (int) v, v -> c.filterB = (int) v);
            }
            default -> {
                toggle(y, "Vinyet", () -> c.vignetteEnabled, v -> c.vignetteEnabled = v);
                slider(x0, y + 24, cw, "Güç %", 0, 100, c.vignetteStrength, true, v -> c.vignetteStrength = (float) v);
                colorRow(y + 48, c.vigR, c.vigG, c.vigB, v -> c.vigR = (int) v, v -> c.vigG = (int) v, v -> c.vigB = (int) v);
            }
        }
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "AÇIK" : "KAPALI"));
    }

    private void toggle(int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x0, y, cw, 20).build());
    }

    private void slider(int x, int y, int w, String label, double min, double max, double cur,
                        boolean whole, DoubleConsumer set) {
        addRenderableWidget(new Sl(x, y, w, label, min, max, cur, whole, set));
    }

    private void colorRow(int y, int r, int g, int b, DoubleConsumer sr, DoubleConsumer sg, DoubleConsumer sb) {
        int w = (cw - 4) / 3;
        slider(x0, y, w, "R", 0, 255, r, true, sr);
        slider(x0 + w + 2, y, w, "G", 0, 255, g, true, sg);
        slider(x0 + (w + 2) * 2, y, w, "B", 0, 255, b, true, sb);
    }

    @Override
    public void removed() {
        ExtrasConfig.save();
    }

    @Override
    public void onClose() {
        ExtrasConfig.save();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    private static class Sl extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final DoubleConsumer set;
        private final boolean whole;

        Sl(int x, int y, int w, String label, double min, double max, double cur,
           boolean whole, DoubleConsumer set) {
            super(x, y, w, 20, Component.empty(), (cur - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.set = set;
            this.whole = whole;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            double v = min + value * (max - min);
            setMessage(Component.literal(label + ": " + (whole ? String.valueOf(Math.round(v)) : String.format("%.1f", v))));
        }

        @Override
        protected void applyValue() {
            set.accept(min + value * (max - min));
        }
    }
}
