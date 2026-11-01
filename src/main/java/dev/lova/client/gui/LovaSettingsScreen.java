package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class LovaSettingsScreen extends Screen {
    private static final int LIST_TOP = 28;
    private static final int BAR_W = 4;

    private final Screen parent;
    private final List<AbstractWidget> items = new ArrayList<>();
    private final List<Integer> offsets = new ArrayList<>();
    private double scroll = 0;
    private int dragMode = 0; // 0 yok, 1 çubuk, 2 içerik
    private int contentH = 0;
    private int listX = 0;
    private int listW = 0;

    public LovaSettingsScreen(Screen parent) {
        super(Component.literal("Lova Client"));
        this.parent = parent;
    }

    private static Component noHurtCamLabel() {
        return Component.literal("Hurt Cam kontrolü: " + (LovaConfig.INSTANCE.noHurtCam ? "AÇIK" : "KAPALI"));
    }

    private int listBottom() {
        return this.height - 32;
    }

    private int maxScroll() {
        return Math.max(0, contentH - (listBottom() - LIST_TOP));
    }

    private int thumbHeight() {
        int trackH = listBottom() - LIST_TOP;
        return Math.max(16, trackH * trackH / Math.max(1, contentH));
    }

    private void addItem(AbstractWidget widget, int offset) {
        this.addRenderableWidget(widget);
        items.add(widget);
        offsets.add(offset);
    }

    private void applyScroll() {
        int bottom = listBottom();
        for (int i = 0; i < items.size(); i++) {
            AbstractWidget wd = items.get(i);
            int y = LIST_TOP + offsets.get(i) - (int) Math.round(scroll);
            wd.setY(y);
            wd.visible = y >= LIST_TOP && y + wd.getHeight() <= bottom;
        }
    }

    private void setScroll(double v) {
        scroll = Math.max(0, Math.min(maxScroll(), v));
        applyScroll();
    }

    private void dragBar(double my) {
        int trackH = listBottom() - LIST_TOP;
        int thumbH = thumbHeight();
        double ratio = (my - LIST_TOP - thumbH / 2.0) / Math.max(1, trackH - thumbH);
        setScroll(ratio * maxScroll());
    }

    @Override
    protected void init() {
        items.clear();
        offsets.clear();

        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        listX = x;
        listW = w;

        Button header = Button.builder(Component.literal("Lova Client Ayarları"), b -> {})
                .bounds(x, 4, w, h).build();
        header.active = false;
        this.addRenderableWidget(header);

        addItem(Button.builder(noHurtCamLabel(), b -> {
            cfg.noHurtCam = !cfg.noHurtCam;
            LovaConfig.save();
            b.setMessage(noHurtCamLabel());
        }).bounds(x, 0, w, h).build(), 0);
                addItem(Button.builder(Component.literal("Tuş gösterici ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new KeystrokesScreen(this));
        }).bounds(x, 0, w, h).build(), 144);
        addItem(Button.builder(Component.literal("Hasar göstergesi ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new DamageIndicatorScreen(this));
        }).bounds(x, 0, w, h).build(), 168);
        addItem(Button.builder(Component.literal("HUD yerleştir"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new HudEditScreen(this));
        }).bounds(x, 0, w, h).build(), 192);
        addItem(Button.builder(Component.literal("Jeton modu ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new CoinScreen(this));
        }).bounds(x, 0, w, h).build(), 216);

        addItem(new ValueSlider(x, 0, w, h, "Sallanma",
                0, 1, cfg.hurtCamStrength, 100, "%",
                v -> cfg.hurtCamStrength = v), 24);

        addItem(Button.builder(Component.literal("Crosshair ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new CrosshairScreen(this));
        }).bounds(x, 0, w, h).build(), 48);

        addItem(Button.builder(Component.literal("Damage Tint ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new DamageTintScreen(this));
        }).bounds(x, 0, w, h).build(), 72);

        addItem(Button.builder(Component.literal("HUD ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new HudScreen(this));
        }).bounds(x, 0, w, h).build(), 96);

        addItem(Button.builder(Component.literal("Kombo ayarları"), b -> {
            LovaConfig.save();
            this.minecraft.setScreen(new ComboScreen(this));
        }).bounds(x, 0, w, h).build(), 120);
        addItem(Button.builder(Component.literal("Ekran efektleri ayarları"), b -> {
LovaConfig.save();
this.minecraft.setScreen(new ScreenEffectsScreen(this));
}).bounds(x, 0, w, h).build(), 240);
        addItem(Button.builder(Component.literal("Zoom ayarları"), b -> {
LovaConfig.save();
this.minecraft.setScreen(new ZoomScreen(this));
}).bounds(x, 0, w, h).build(), 264);
addItem(Button.builder(Component.literal("Sunucu bilgisi ayarları"), b -> {
LovaConfig.save();
this.minecraft.setScreen(new ServerInfoScreen(this));
}).bounds(x, 0, w, h).build(), 288);
        addItem(Button.builder(Component.literal("Sohbet zaman damgası"), b -> {
LovaConfig.save();
this.minecraft.setScreen(new ChatTimestampScreen(this));
}).bounds(x, 0, w, h).build(), 312);

addItem(Button.builder(Component.literal("Waypoint ayarları"), b -> {
LovaConfig.save();
this.minecraft.setScreen(new WaypointScreen(this));
}).bounds(x, 0, w, h).build(), 336);
contentH = 336 + h;

        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(x, this.height - 28, w, h).build());

        setScroll(scroll);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (maxScroll() > 0 && my >= LIST_TOP && my < listBottom()) {
            if (mx >= listX + listW && mx <= listX + listW + 14) {
                dragMode = 1;
                dragBar(my);
                return true;
            }
            boolean overItem = false;
            for (AbstractWidget wd : items) {
                if (wd.visible && wd.isMouseOver(mx, my)) {
                    overItem = true;
                    break;
                }
            }
            if (!overItem) {
                dragMode = 2;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragMode == 1) {
            dragBar(event.y());
            return true;
        }
        if (dragMode == 2) {
            setScroll(scroll - dragY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragMode = 0;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        setScroll(scroll - scrollY * 20);
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int max = maxScroll();
        if (max > 0) {
            int trackH = listBottom() - LIST_TOP;
            int thumbH = thumbHeight();
            int tx = listX + listW + 4;
            int ty = LIST_TOP + (int) Math.round((trackH - thumbH) * (scroll / max));
            g.fill(tx, LIST_TOP, tx + BAR_W, LIST_TOP + trackH, 0x55000000);
            g.fill(tx, ty, tx + BAR_W, ty + thumbH, 0xFFAAAAAA);
        }
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
