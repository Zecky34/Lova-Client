package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class StrengthSlider extends AbstractSliderButton {
    public StrengthSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), LovaConfig.INSTANCE.hurtCamStrength);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal("Sallanma: " + (int) Math.round(this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
        LovaConfig.INSTANCE.hurtCamStrength = this.value;
    }
}
