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
