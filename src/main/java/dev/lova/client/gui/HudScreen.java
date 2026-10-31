package dev.lova.client.gui;

import dev.lova.client.LovaConfig;
import dev.lova.client.feature.HudFeature;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class HudScreen extends Screen {
    private static final String[] KEY_CORNERS = {"Sol üst", "Sağ üst", "Sol alt", "Sağ alt"};
    private static final int CONTENT_BOTTOM = 28 + 22 * 19 + 4 + 20;

    private final Screen parent;
    private int scroll = 0;

    public HudScreen(Screen parent) {
        super(Component.literal("HUD"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "AÇIK" : "KAPALI"));
    }

    private static Component cornerLabel(int corner) {
        int len = HudFeature.CORNER_NAMES.length;
        return Component.literal("Konum: " + HudFeature.CORNER_NAMES[Math.floorMod(corner, len)]);
    }

    private static Component keysCornerLabel(int corner) {
        return Component.literal("Tuş konumu: " + KEY_CORNERS[Math.floorMod(corner, 4)]);
    }

    private static int withChannel(int rgb, int shift, int value) {
        int v = Math.max(0, Math.min(255, value));
        return (rgb & ~(0xFF << shift)) | (v << shift);
    }

    private Button toggle(int x, int y, int w, int h, String name,
                          BooleanSupplier get, Consumer<Boolean> set) {
        return Button.builder(onOff(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            LovaConfig.save();
            b.setMessage(onOff(name, get.getAsBoolean()));
        }).bounds(x, y, w, h).build();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = Math.max(0, CONTENT_BOTTOM - this.height + 4);
        int next = Math.max(0, Math.min(max, scroll - (int) (scrollY * 22)));
        if (next != scroll) {
            scroll = next;
            this.rebuildWidgets();
        }
        return true;
    }

    @Override
    protected void init() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        int w = 150;
        int h = 20;
        int step = 22;
        int lx = this.width / 2 - 155;
        int rx = this.width / 2 + 5;
        int y0 = 28 - scroll;

        // Sol sütun
        this.addRenderableWidget(toggle(lx, y0, w, h, "HUD",
                () -> cfg.hudEnabled, v -> cfg.hudEnabled = v));
        this.addRenderableWidget(toggle(lx, y0 + step, w, h, "FPS",
                () -> cfg.hudFps, v -> cfg.hudFps = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 2, w, h, "CPS",
                () -> cfg.hudCps, v -> cfg.hudCps = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 3, w, h, "Koordinat",
                () -> cfg.hudCoords, v -> cfg.hudCoords = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 4, w, h, "Ping",
                () -> cfg.hudPing, v -> cfg.hudPing = v));
        this.addRenderableWidget(toggle(lx, y0 + step * 5, w, h, "Totem",
                () -> cfg.hudTotem, v -> cfg.hudTotem = v));

        // Sağ sütun
        this.addRenderableWidget(toggle(rx, y0, w, h, "Zırh",
                () -> cfg.hudArmor, v -> cfg.hudArmor = v));
        this.addRenderableWidget(toggle(rx, y0 + step, w, h, "Efektler",
                () -> cfg.hudEffects, v -> cfg.hudEffects = v));
        this.addRenderableWidget(toggle(rx, y0 + step * 2, w, h, "Arka plan",
                () -> cfg.hudBackground, v -> cfg.hudBackground = v));

        this.addRenderableWidget(Button.builder(cornerLabel(cfg.hudCorner), b -> {
            int len = HudFeature.CORNER_NAMES.length;
            cfg.hudCorner = Math.floorMod(cfg.hudCorner + 1, len);
            LovaConfig.save();
            b.setMessage(cornerLabel(cfg.hudCorner));
        }).bounds(rx, y0 + step * 3, w, h).build());

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 4, w, h, "Kırmızı",
                0, 255, (cfg.hudColor >> 16) & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 16, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 5, w, h, "Yeşil",
                0, 255, (cfg.hudColor >> 8) & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 8, (int) Math.round(v))));

        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 6, w, h, "Mavi",
                0, 255, cfg.hudColor & 0xFF, 1, "",
                v -> cfg.hudColor = withChannel(cfg.hudColor, 0, (int) Math.round(v))));

        // Kombo (sol, satır 8-12)
        this.addRenderableWidget(toggle(lx, y0 + step * 8, w, h, "Kombo",
                () -> cfg.comboEnabled, v -> cfg.comboEnabled = v));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 9, w, h, "Sıfırlanma",
                1, 10, cfg.comboResetSec, 1, " sn",
                v -> cfg.comboResetSec = v));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 10, w, h, "Kombo kırmızı",
                0, 255, (cfg.comboRgb >> 16) & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 16, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 11, w, h, "Kombo yeşil",
                0, 255, (cfg.comboRgb >> 8) & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 8, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 12, w, h, "Kombo mavi",
                0, 255, cfg.comboRgb & 0xFF, 1, "",
                v -> cfg.comboRgb = withChannel(cfg.comboRgb, 0, (int) Math.round(v))));

        // Tuş gösterici (sağ, satır 8-13)
        this.addRenderableWidget(toggle(rx, y0 + step * 8, w, h, "Tuş gösterici",
                () -> cfg.keysEnabled, v -> cfg.keysEnabled = v));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 9, w, h, "Tuş boyutu",
                0.5, 2.5, cfg.keysScale, 100, "%",
                v -> cfg.keysScale = v));
        this.addRenderableWidget(Button.builder(keysCornerLabel(cfg.keysCorner), b -> {
            cfg.keysCorner = Math.floorMod(cfg.keysCorner + 1, 4);
            LovaConfig.save();
            b.setMessage(keysCornerLabel(cfg.keysCorner));
        }).bounds(rx, y0 + step * 10, w, h).build());
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 11, w, h, "Tuş kırmızı",
                0, 255, (cfg.keysRgb >> 16) & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 16, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 12, w, h, "Tuş yeşil",
                0, 255, (cfg.keysRgb >> 8) & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 8, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 13, w, h, "Tuş mavi",
                0, 255, cfg.keysRgb & 0xFF, 1, "",
                v -> cfg.keysRgb = withChannel(cfg.keysRgb, 0, (int) Math.round(v))));

        // Ekran filtresi (sol, satır 14-18)
        this.addRenderableWidget(toggle(lx, y0 + step * 14, w, h, "Ekran filtresi",
                () -> cfg.filterEnabled, v -> cfg.filterEnabled = v));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 15, w, h, "Filtre gücü",
                0, 1, cfg.filterStrength, 100, "%",
                v -> cfg.filterStrength = v));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 16, w, h, "Filtre kırmızı",
                0, 255, (cfg.filterRgb >> 16) & 0xFF, 1, "",
                v -> cfg.filterRgb = withChannel(cfg.filterRgb, 16, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 17, w, h, "Filtre yeşil",
                0, 255, (cfg.filterRgb >> 8) & 0xFF, 1, "",
                v -> cfg.filterRgb = withChannel(cfg.filterRgb, 8, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(lx, y0 + step * 18, w, h, "Filtre mavi",
                0, 255, cfg.filterRgb & 0xFF, 1, "",
                v -> cfg.filterRgb = withChannel(cfg.filterRgb, 0, (int) Math.round(v))));

        // Vinyet (sağ, satır 14-18)
        this.addRenderableWidget(toggle(rx, y0 + step * 14, w, h, "Vinyet",
                () -> cfg.vignetteEnabled, v -> cfg.vignetteEnabled = v));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 15, w, h, "Vinyet gücü",
                0, 1, cfg.vignetteStrength, 100, "%",
                v -> cfg.vignetteStrength = v));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 16, w, h, "Vinyet kırmızı",
                0, 255, (cfg.vignetteRgb >> 16) & 0xFF, 1, "",
                v -> cfg.vignetteRgb = withChannel(cfg.vignetteRgb, 16, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 17, w, h, "Vinyet yeşil",
                0, 255, (cfg.vignetteRgb >> 8) & 0xFF, 1, "",
                v -> cfg.vignetteRgb = withChannel(cfg.vignetteRgb, 8, (int) Math.round(v))));
        this.addRenderableWidget(new ValueSlider(rx, y0 + step * 18, w, h, "Vinyet mavi",
                0, 255, cfg.vignetteRgb & 0xFF, 1, "",
                v -> cfg.vignetteRgb = withChannel(cfg.vignetteRgb, 0, (int) Math.round(v))));

        // Geri
        this.addRenderableWidget(Button.builder(Component.literal("Geri"), b -> this.onClose())
                .bounds(this.width / 2 - 75, y0 + step * 19 + 4, 150, h).build());

        // Başlık en son eklenir, kaydırınca üstte kalsın
        Button header = Button.builder(Component.literal("HUD ayarları"), b -> {})
                .bounds(this.width / 2 - 100, 4, 200, 20).build();
        header.active = false;
        this.addRenderableWidget(header);
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
