package dev.lova.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

public class ValueSlider extends AbstractSliderButton {
    private final String label;
    private final double min;
    private final double max;
    private final double displayScale;
    private final String suffix;
    private final DoubleConsumer onChange;

    public ValueSlider(int x, int y, int width, int height, String label,
                       double min, double max, double current,
                       double displayScale, String suffix, DoubleConsumer onChange) {
        super(x, y, width, height, Component.empty(),
                Math.max(0.0, Math.min(1.0, (current - min) / (max - min))));
        this.label = label;
        this.min = min;
        this.max = max;
        this.displayScale = displayScale;
        this.suffix = suffix;
        this.onChange = onChange;
        this.updateMessage();
    }

    private double actual() {
        return this.min + this.value * (this.max - this.min);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(this.label + ": "
                + (int) Math.round(this.actual() * this.displayScale) + this.suffix));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this.actual());
    }
}
