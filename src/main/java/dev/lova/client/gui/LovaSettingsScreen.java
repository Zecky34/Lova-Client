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
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 4;

        Button header = Button.builder(Component.literal("Lova Client Ayarları"), b -> {})
                .bounds(x, y, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        this.addRenderableWidget(Button.builder(noHurtCamLabel(), b -> {
            LovaConfig.INSTANCE.noHurtCam = !LovaConfig.INSTANCE.noHurtCam;
            LovaConfig.save();
            b.setMessage(noHurtCamLabel());
        }).bounds(x, y + 30, w, h).build());

        this.addRenderableWidget(new StrengthSlider(x, y + 60, w, h));

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, y + 120, w, h).build());
    }

    @Override
    public void removed() {
        LovaConfig.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
