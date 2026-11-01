package dev.lova.client.gui;

import dev.lova.client.feature.WaypointFeature;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WaypointListScreen extends Screen {

    private static final int PER_PAGE = 5;

    private final Screen parent;
    private final int page;

    public WaypointListScreen(Screen parent, int page) {
        super(Component.literal("Noktalar"));
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected void init() {
        List<WaypointFeature.Waypoint> list = WaypointFeature.points();
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        int cur = Math.max(0, Math.min(pages - 1, page));
        int w = 200;
        int h = 20;
        int step = 22;
        int x = this.width / 2 - w / 2;
        int y0 = 28;

        Button header = Button.builder(Component.literal("Noktalar (" + list.size() + ")"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        int start = cur * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
            WaypointFeature.Waypoint wp = list.get(start + i);
            String label = wp.name == null || wp.name.isEmpty() ? "(adsız)" : wp.name;
            this.addRenderableWidget(Button.builder(Component.literal(label), b ->
                    this.minecraft.setScreen(new WaypointEditScreen(this, wp, false)))
                    .bounds(x, y0 + step * i, w, h).build());
        }

        int ny = y0 + step * PER_PAGE + 4;
        Button prev = Button.builder(Component.literal("<"), b ->
                this.minecraft.setScreen(new WaypointListScreen(parent, cur - 1)))
                .bounds(x, ny, 60, h).build();
        prev.active = cur > 0;
        this.addRenderableWidget(prev);

        Button label = Button.builder(Component.literal((cur + 1) + " / " + pages), b -> {})
                .bounds(x + 62, ny, 76, h).build();
        label.active = false;
        this.addRenderableWidget(label);

        Button next = Button.builder(Component.literal(">"), b ->
                this.minecraft.setScreen(new WaypointListScreen(parent, cur + 1)))
                .bounds(x + 140, ny, 60, h).build();
        next.active = cur < pages - 1;
        this.addRenderableWidget(next);

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, ny + step + 4, w, h).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
