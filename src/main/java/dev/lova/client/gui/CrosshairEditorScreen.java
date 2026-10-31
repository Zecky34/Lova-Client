package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CrosshairEditorScreen extends Screen {
    private final Screen parent;
    private final char[] pixels;
    private final Button[] cells = new Button[LovaConfig.GRID * LovaConfig.GRID];

    public CrosshairEditorScreen(Screen parent) {
        super(Component.literal("Özel crosshair"));
        this.parent = parent;
        String saved = LovaConfig.INSTANCE.crosshairCustomPixels;
        if (saved == null || saved.length() != LovaConfig.GRID * LovaConfig.GRID) {
            saved = LovaConfig.defaultPixels();
        }
        this.pixels = saved.toCharArray();
    }

    private Component cellLabel(int idx) {
        return Component.literal(this.pixels[idx] == '1' ? "X" : "");
    }

    private void refreshAll() {
        for (int i = 0; i < this.cells.length; i++) {
            if (this.cells[i] != null) {
                this.cells[i].setMessage(cellLabel(i));
            }
        }
    }

    private void commit() {
        LovaConfig.INSTANCE.crosshairCustomPixels = new String(this.pixels);
        LovaConfig.save();
    }

    @Override
    protected void init() {
        int grid = LovaConfig.GRID;
        int cell = 14;
        int total = grid * cell;
        int x0 = this.width / 2 - total / 2;
        int y0 = 26;

        Button header = Button.builder(Component.literal("Özel crosshair: karelere dokun"), b -> {})
                .bounds(this.width / 2 - 100, 2, 200, 20).build();
        header.active = false;
        this.addRenderableWidget(header);

        for (int y = 0; y < grid; y++) {
            for (int x = 0; x < grid; x++) {
                final int idx = y * grid + x;
                Button cellButton = Button.builder(cellLabel(idx), btn -> {
                    this.pixels[idx] = this.pixels[idx] == '1' ? '0' : '1';
                    btn.setMessage(cellLabel(idx));
                }).bounds(x0 + x * cell, y0 + y * cell, cell, cell).build();
                this.cells[idx] = cellButton;
                this.addRenderableWidget(cellButton);
            }
        }

        int by = y0 + total + 6;
        int bw = 60;
        int gap = 4;
        int bx = this.width / 2 - (3 * bw + 2 * gap) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Temizle"), b -> {
            for (int i = 0; i < this.pixels.length; i++) {
                this.pixels[i] = '0';
            }
            refreshAll();
        }).bounds(bx, by, bw, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Varsayılan"), b -> {
            char[] def = LovaConfig.defaultPixels().toCharArray();
            System.arraycopy(def, 0, this.pixels, 0, this.pixels.length);
            refreshAll();
        }).bounds(bx + bw + gap, by, bw, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Kaydet"), b -> {
            LovaConfig.INSTANCE.crosshairStyle = 4;
            this.onClose();
        }).bounds(bx + 2 * (bw + gap), by, bw, 20).build());
    }

    @Override
    public void removed() {
        commit();
    }

    @Override
    public void onClose() {
        commit();
        this.minecraft.setScreen(parent);
    }
}
