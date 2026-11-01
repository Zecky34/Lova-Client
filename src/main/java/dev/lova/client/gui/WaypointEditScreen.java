package dev.lova.client.gui;

import dev.lova.client.feature.WaypointFeature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WaypointEditScreen extends Screen {

    private final Screen parent;
    private final WaypointFeature.Waypoint wp;
    private final boolean isNew;
    private EditBox nameBox;
    private int cr;
    private int cg;
    private int cb;

    public WaypointEditScreen(Screen parent, WaypointFeature.Waypoint wp, boolean isNew) {
        super(Component.literal("Nokta"));
        this.parent = parent;
        this.wp = wp;
        this.isNew = isNew;
        this.cr = (wp.rgb >> 16) & 0xFF;
        this.cg = (wp.rgb >> 8) & 0xFF;
        this.cb = wp.rgb & 0xFF;
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(
                Component.literal(isNew ? "Yeni nokta" : "Noktayı düzenle"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        nameBox = new EditBox(this.minecraft.font, x, y0, w, h, Component.literal("Ad"));
        nameBox.setMaxLength(24);
        nameBox.setValue(wp.name == null ? "" : wp.name);
        this.addRenderableWidget(nameBox);

        this.addRenderableWidget(new ValueSlider(x, y0 + step, w, h, "Kırmızı",
                0, 255, cr, 1, "", v -> cr = (int) Math.round(v)));
        this.addRenderableWidget(new ValueSlider(x, y0 + step * 2, w, h, "Yeşil",
                0, 255, cg, 1, "", v -> cg = (int) Math.round(v)));
        this.addRenderableWidget(new ValueSlider(x, y0 + step * 3, w, h, "Mavi",
                0, 255, cb, 1, "", v -> cb = (int) Math.round(v)));

        int by = y0 + step * 4 + 12;
        this.addRenderableWidget(Button.builder(Component.literal("Kaydet"), b -> {
            String n = nameBox.getValue().trim();
            if (!n.isEmpty()) {
                wp.name = n;
            }
            wp.rgb = (cr << 16) | (cg << 8) | cb;
            if (isNew) {
                WaypointFeature.points().add(wp);
            }
            WaypointFeature.save();
            this.onClose();
        }).bounds(x, by, w, h).build());

        int next = by + step;
        if (!isNew) {
            this.addRenderableWidget(Button.builder(Component.literal("Sil"), b -> {
                WaypointFeature.points().remove(wp);
                WaypointFeature.save();
                this.onClose();
            }).bounds(x, next, w, h).build());
            next += step;
        }
        this.addRenderableWidget(Button.builder(Component.literal("İptal"), b -> this.onClose())
                .bounds(x, next, w, h).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x = this.width / 2 - 100;
        int y = 28 + 22 * 4 + 1;
        int col = 0xFF000000 | (cr << 16) | (cg << 8) | cb;
        g.fill(x, y, x + 200, y + 8, col);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
