package dev.lova.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class LovaConfig {
    public static final int GRID = 11;

    public boolean noHurtCam = true;
    public double hurtCamStrength = 0.0;

    public boolean crosshairEnabled = false;
    public int crosshairStyle = 0;
    public int crosshairColor = 0xFFFFFF;
    public int crosshairSize = 5;
    public double crosshairOpacity = 1.0;
    public boolean crosshairTargetEnabled = false;
    public int crosshairTargetColor = 0xFF3030;
    public String crosshairCustomPixels = defaultPixels();

    public boolean damageTintEnabled = false;
    public int damageTintRgb = 0x0060FF;
    public double damageTintOpacity = 0.6;

    public boolean hudEnabled = false;
    public boolean hudFps = true;
    public boolean hudCps = true;
    public boolean hudCoords = true;
    public boolean hudPing = false;
    public boolean hudTotem = false;
    public boolean hudArmor = false;
    public boolean hudEffects = false;
    public boolean hudBackground = true;
    public int hudCorner = 0;
    public int hudColor = 0xFFFFFF;

    // YENİ: Kombo sayacı
    public boolean comboEnabled = false;
    public double comboResetSec = 3.0;
    public double comboScale = 2.0;
    public int comboRgb = 0xFFFFFF;

        // YENİ: Tuş gösterici
    public boolean keysEnabled = false;
    public double keysScale = 1.0;
    public int keysRgb = 0x50C8FF;
        // YENİ: Hasar göstergesi
    public boolean damageIndicatorEnabled = false;
    public double damageIndicatorSec = 1.0;
    public double damageIndicatorScale = 1.0;
    public int damageIndicatorRgb = 0xFF3030;
        // YENİ: HUD yerleştirme (-1 = varsayılan konum)
    public double hudPosX = -1;
    public double hudPosY = -1;
    public double hudScale = 1.0;
    public double comboPosX = -1;
    public double comboPosY = -1;
    public double keysPosX = -1;
    public double keysPosY = -1;
        // YENİ: HUD elemanları ayrı ayrı
    // sıra: FPS, CPS, Koordinat, Ping, Totem, Kask, Göğüslük, Pantolon, Bot, Efektler
    public double[] hudPartPosX = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    public double[] hudPartPosY = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    public double[] hudPartScale = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        // YENİ: Jeton modu
    public boolean coinEnabled = false;
    public double coinScale = 1.0;
    public double coinGroundSec = 1.5;
    public double coinFall = 1.0;
    public int coinTheme = 0;
    public int coinMax = 10;
    // YENİ: Ekran efektleri
public boolean grainEnabled = false;
public double grainStrength = 0.0;
public int grainMode = 0;

    public static LovaConfig INSTANCE = new LovaConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String defaultPixels() {
        StringBuilder sb = new StringBuilder();
        int mid = GRID / 2;
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                sb.append((x == mid || y == mid) ? '1' : '0');
            }
        }
        return sb.toString();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("lovaclient.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    LovaConfig c = GSON.fromJson(r, LovaConfig.class);
                    if (c != null) {
                        INSTANCE = c;
                    }
                }
            }
        } catch (Exception e) {
            LovaClient.LOGGER.error("Config could not be loaded", e);
        }
        if (INSTANCE.crosshairCustomPixels == null || INSTANCE.crosshairCustomPixels.length() != GRID * GRID) {
            INSTANCE.crosshairCustomPixels = defaultPixels();
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LovaClient.LOGGER.error("Config could not be saved", e);
        }
    }
}
