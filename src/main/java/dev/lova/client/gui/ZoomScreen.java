package dev.lova.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lova.client.LovaConfig;
import dev.lova.client.feature.ZoomFeature;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ZoomScreen extends Screen {

    private final Screen parent;
    private Button keyButton;
    private boolean waiting = false;

    public ZoomScreen(Screen parent) {
        super(Component.literal("Zoom"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Zoom: " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component smoothLabel(boolean value) {
        return Component.literal("Yumuşak geçiş: " + (value ? "AÇIK" : "KAPALI"));
    }

    private Component keyLabel() {
        if (waiting) {
            return Component.literal("Zoom tuşu: > bir tuşa bas <");
        }
        KeyMapping k = ZoomFeature.getKey();
        return Component.literal("Zoom tuşu: ").append(k.getTranslatedKeyMessage());
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Zoom ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.zoomEnabled), b -> {
            cfg.zoomEnabled = !cfg.zoomEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.zoomEnabled));
        }).bounds(x, y0, w, h).build());

        keyButton = Button.builder(keyLabel(), b -> {
            waiting = true;
            b.setMessage(keyLabel());
        }).bounds(x, y0 + step, w, h).build();
        this.addRenderableWidget(keyButton);

        this.addRenderableWidget(Button.builder(smoothLabel(cfg.zoomSmooth), b -> {
            cfg.zoomSmooth = !cfg.zoomSmooth;
            LovaConfig.save();
            b.setMessage(smoothLabel(cfg.zoomSmooth));
        }).bounds(x, y0 + step * 2, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 3, w, h, "Görüş açısı",
                0.1, 0.8, cfg.zoomFov, 100, "%",
                v -> cfg.zoomFov = v));

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y0 + step * 4 + 8, w, h).build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (waiting) {
            waiting = false;
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) {
                KeyMapping k = ZoomFeature.getKey();
                k.setKey(InputConstants.Type.KEYSYM.getOrCreate(event.key()));
                KeyMapping.resetMapping();
                this.minecraft.options.save();
            }
            keyButton.setMessage(keyLabel());
            return true;
        }
        return super.keyPressed(event);
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
