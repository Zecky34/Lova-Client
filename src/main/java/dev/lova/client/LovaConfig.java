package dev.lova.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class LovaConfig {
    public boolean noHurtCam = true;
    public double hurtCamStrength = 0.0;

    public static LovaConfig INSTANCE = new LovaConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
