package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LovaSettingsScreen extends Screen {
    private final Screen parent;

    public LovaSettingsScreen(Screen parent) {
        super(Component.literal("Lova Client"));
        this.parent = parent;
    }

    private static Component noHurtCamLabel() {
        return Component.literal("Hurt Cam kontrolü: " + (LovaConfig.INSTANCE.noHurtCam ? "AÇIK" : "KAPALI"));
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 4;

        Button header = Button.builder(Component.literal("Lova Client Ayarları"), b -> {})
                .bounds(x, y, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(noHurtCamLabel(), b -> {
            cfg.noHurtCam = !cfg.noHurtCam;
            LovaConfig.save();
            b.setMessage(noHurtCamLabel());
        }).bounds(x, y + 24, w, h).build());

        this.addRenderableWidget(new ValueSlider(x, y + 48, w, h, "Sallanma",
                0, 1, cfg.hurtCamStrength, 100, "%",
                v -> cfg.hurtCamStrength = v));

        this.addRenderableWidget(Button.builder(Component.literal("Crosshair ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new CrosshairScreen(this));
        }).bounds(x, y + 72, w, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("Damage Tint ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new DamageTintScreen(this));
        }).bounds(x, y + 96, w, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("HUD ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new HudScreen(this));
        }).bounds(x, y + 120, w, h).build());

    @Override
    public void removed() {
        LovaConfig.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
