package dev.lova.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lova.client.LovaConfig;
import dev.lova.client.feature.WaypointFeature;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class WaypointScreen extends Screen {

    private final Screen parent;
    private Button keyButton;
    private boolean waiting = false;

    public WaypointScreen(Screen parent) {
        super(Component.literal("Waypoint"));
        this.parent = parent;
    }

    private static Component onOff(boolean value) {
        return Component.literal("Waypoint: " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component deathLabel(boolean value) {
        return Component.literal("Son ölüm: " + (value ? "AÇIK" : "KAPALI"));
    }

    private Component keyLabel() {
        if (waiting) {
            return Component.literal("Nokta tuşu: > bir tuşa bas <");
        }
        KeyMapping k = WaypointFeature.getKey();
        return Component.literal("Nokta tuşu: ").append(k.getTranslatedKeyMessage());
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Waypoint ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(onOff(cfg.waypointEnabled), b -> {
            cfg.waypointEnabled = !cfg.waypointEnabled;
            LovaConfig.save();
            b.setMessage(onOff(cfg.waypointEnabled));
        }).bounds(x, y0, w, h).build());

        keyButton = Button.builder(keyLabel(), b -> {
            waiting = true;
            b.setMessage(keyLabel());
        }).bounds(x, y0 + step, w, h).build();
        this.addRenderableWidget(keyButton);

        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Boyut",
                0.0, 2.0, cfg.waypointScale, 100, "%",
                v -> cfg.waypointScale = v));

        this.addRenderableWidget(Button.builder(deathLabel(cfg.waypointDeathEnabled), b -> {
            cfg.waypointDeathEnabled = !cfg.waypointDeathEnabled;
            LovaConfig.save();
            b.setMessage(deathLabel(cfg.waypointDeathEnabled));
        }).bounds(x, y0 + step * 3, 130, h).build());

        Button clearDeath = Button.builder(Component.literal("Sil"), b -> {
            WaypointFeature.clearDeath();
            b.active = false;
        }).bounds(x + 132, y0 + step * 3, 68, h).build();
        clearDeath.active = WaypointFeature.death() != null;
        this.addRenderableWidget(clearDeath);

        Button add = Button.builder(Component.literal("Şu anki konumu ekle"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new WaypointEditScreen(this,
                    WaypointFeature.newPoint(this.minecraft), true));
        }).bounds(x, y0 + step * 4, w, h).build();
        add.active = this.minecraft.player != null && this.minecraft.level != null;
        this.addRenderableWidget(add);

        this.addRenderableWidget(Button.builder(
                Component.literal("Noktalar (" + WaypointFeature.points().size() + ")"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new WaypointListScreen(this, 0));
        }).bounds(x, y0 + step * 5, w, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y0 + step * 6 + 8, w, h).build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (waiting) {
            waiting = false;
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) {
                KeyMapping k = WaypointFeature.getKey();
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
