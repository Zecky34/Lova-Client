package dev.lova.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ExtrasConfig {
    // Kombo
    public boolean comboEnabled = false;
    public float comboResetSec = 3f;
    public float comboScale = 2f;
    public int comboR = 255, comboG = 255, comboB = 255;
    // Tuş gösterici
    public boolean keysEnabled = false;
    public float keysScale = 1f;
    public int keysCorner = 3; // 0 sol üst, 1 sağ üst, 2 sol alt, 3 sağ alt
    public int keysR = 80, keysG = 200, keysB = 255;
    // Ekran filtresi
    public boolean filterEnabled = false;
    public float filterStrength = 20f;
    public int filterR = 255, filterG = 120, filterB = 0;
    // Vinyet
    public boolean vignetteEnabled = false;
    public float vignetteStrength = 50f;
    public int vigR = 0, vigG = 0, vigB = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ExtrasConfig inst;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("lovaclient-extras.json");
    }

    public static ExtrasConfig get() {
        if (inst == null) {
            try {
                Path p = path();
                if (Files.exists(p)) inst = GSON.fromJson(Files.readString(p), ExtrasConfig.class);
            } catch (Exception ignored) {
            }
            if (inst == null) inst = new ExtrasConfig();
        }
        return inst;
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(get()));
        } catch (Exception ignored) {
        }
    }
}
