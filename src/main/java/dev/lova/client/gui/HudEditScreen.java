package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import dev.lova.client.feature.ComboFeature;
import dev.lova.client.feature.HudFeature;
import dev.lova.client.feature.KeystrokesFeature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HudEditScreen extends Screen {
    private static final int HUD = 0;
    private static final int COMBO = 1;
    private static final int KEYS = 2;
    private static final String[] NAMES = {"HUD", "Kombo", "Tuş gösterici"};
    private static final int SNAP = 4;
    private static final int EDGE = 4;

    private final Screen parent;
    private int selected = -1;
    private int dragging = -1;
    private double grabX = 0;
    private double grabY = 0;
    private int snapX = -1;
    private int snapY = -1;

    public HudEditScreen(Screen parent) {
        super(Component.literal("HUD yerleştir"));
        this.parent = parent;
    }

    private int[] box(int i) {
        return switch (i) {
            case HUD -> HudFeature.bounds(this.minecraft, this.width, this.height);
            case COMBO -> ComboFeature.bounds(this.minecraft, this.width, this.height);
            default -> KeystrokesFeature.bounds(this.width, this.height);
        };
    }

    private boolean enabled(int i) {
        LovaConfig c = LovaConfig.INSTANCE;
        return i == HUD ? c.hudEnabled : i == COMBO ? c.comboEnabled : c.keysEnabled;
    }

    private double getScale(int i) {
        LovaConfig c = LovaConfig.INSTANCE;
        return i == HUD ? c.hudScale : i == COMBO ? c.comboScale : c.keysScale;
    }

    private void setScale(int i, double v) {
        LovaConfig c = LovaConfig.INSTANCE;
        if (i == HUD) {
            c.hudScale = v;
        } else if (i == COMBO) {
            c.comboScale = v;
        } else {
            c.keysScale = v;
        }
    }

    private void setPos(int i, double x, double y, int w, int h) {
        LovaConfig c = LovaConfig.INSTANCE;
        double fx = this.width - w > 0 ? Math.max(0, Math.min(1, x / (this.width - w))) : 0;
        double fy = this.height - h > 0 ? Math.max(0, Math.min(1, y / (this.height - h))) : 0;
        if (i == HUD) {
            c.hudPosX = fx;
            c.hudPosY = fy;
        } else if (i == COMBO) {
            c.comboPosX = fx;
            c.comboPosY = fy;
        } else {
            c.keysPosX = fx;
            c.keysPosY = fy;
        }
    }

    private void changeScale(double delta) {
        if (selected < 0) {
            return;
        }
        double v = getScale(selected) + delta;
        v = Math.round(v * 10.0) / 10.0;
        v = Math.max(0.3, Math.min(2.0, v));
        setScale(selected, v);
        LovaConfig.save();
    }

    private void resetPositions() {
        LovaConfig c = LovaConfig.INSTANCE;
        c.hudPosX = -1;
        c.hudPosY = -1;
        c.comboPosX = -1;
        c.comboPosY = -1;
        c.keysPosX = -1;
        c.keysPosY = -1;
        LovaConfig.save();
    }

    @Override
    protected void init() {
        int bw = Math.min(80, (this.width - 30) / 4);
        int gap = 4;
        int total = bw * 4 + gap * 3;
        int x = (this.width - total) / 2;
        int y = this.height - 28;

        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> changeScale(-0.1))
                .bounds(x, y, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> changeScale(0.1))
                .bounds(x + (bw + gap), y, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Sıfırla"), b -> resetPositions())
                .bounds(x + (bw + gap) * 2, y, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Bitti"), b -> this.onClose())
                .bounds(x + (bw + gap) * 3, y, bw, 20).build());
    }

    private int[] linesFor(boolean xAxis) {
        int total = xAxis ? this.width : this.height;
        List<Integer> l = new ArrayList<>();
        l.add(EDGE);
        l.add(total / 2);
        l.add(total - EDGE);
        for (int i = 0; i < 3; i++) {
            if (i == dragging) {
                continue;
            }
            int[] b = box(i);
            int p = xAxis ? b[0] : b[1];
            int s = xAxis ? b[2] : b[3];
            l.add(p);
            l.add(p + s / 2);
            l.add(p + s);
        }
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = l.get(i);
        }
        return r;
    }

    /** Döner: {düzeltilmiş konum, yapışılan çizgi (yoksa -1)} */
    private double[] snapAxis(double pos, double size, int[] lines) {
        double[] offs = {0, size / 2.0, size};
        double bestAbs = SNAP + 0.5;
        double bestD = 0;
        int bestLine = -1;
        for (int line : lines) {
            for (double off : offs) {
                double d = line - (pos + off);
                if (Math.abs(d) <= SNAP && Math.abs(d) < bestAbs) {
                    bestAbs = Math.abs(d);
                    bestD = d;
                    bestLine = line;
                }
            }
        }
        if (bestLine >= 0) {
            return new double[]{pos + bestD, bestLine};
        }
        return new double[]{pos, -1};
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        double mx = event.x();
        double my = event.y();
        for (int i = 2; i >= 0; i--) {
            int[] b = box(i);
            if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3]) {
                selected = i;
                dragging = i;
                grabX = mx - b[0];
                grabY = my - b[1];
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging < 0) {
            return super.mouseDragged(event, dragX, dragY);
        }
        int[] b = box(dragging);
        int w = b[2];
        int h = b[3];
        double nx = event.x() - grabX;
        double ny = event.y() - grabY;

        double[] sx = snapAxis(nx, w, linesFor(true));
        double[] sy = snapAxis(ny, h, linesFor(false));
        nx = sx[0];
        ny = sy[0];
        snapX = (int) sx[1];
        snapY = (int) sy[1];

        nx = Math.max(0, Math.min(this.width - w, nx));
        ny = Math.max(0, Math.min(this.height - h, ny));
        setPos(dragging, nx, ny, w, h);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean r = super.mouseReleased(event);
        if (dragging >= 0) {
            dragging = -1;
            snapX = -1;
            snapY = -1;
            LovaConfig.save();
            return true;
        }
        return r;
    }

    private static void frame(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Boş: dünya bulanmasın ve kararmasın
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ComboFeature.drawPreview(g, this.minecraft);

        if (snapX >= 0) {
            g.fill(snapX, 0, snapX + 1, this.height, 0xAAFFFF55);
        }
        if (snapY >= 0) {
            g.fill(0, snapY, this.width, snapY + 1, 0xAAFFFF55);
        }

        for (int i = 0; i < 3; i++) {
            int[] b = box(i);
            int color = i == selected ? 0xFF50C8FF : (enabled(i) ? 0xFFFFFFFF : 0x88FFFFFF);
            frame(g, b[0], b[1], b[2], b[3], color);
            String label = NAMES[i] + (enabled(i) ? "" : " (kapalı)");
            int ly = b[1] >= 12 ? b[1] - 10 : b[1] + b[3] + 2;
            g.drawString(this.minecraft.font, label, b[0], ly, color, true);
        }

        super.render(g, mouseX, mouseY, partialTick);

        String info;
        if (selected >= 0) {
            info = "Seçili: " + NAMES[selected] + "  %" + Math.round(getScale(selected) * 100);
        } else {
            info = "Bir elemana bas ve sürükle";
        }
        g.drawString(this.minecraft.font, info,
                (this.width - this.minecraft.font.width(info)) / 2, 6, 0xFFFFFFFF, true);
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
