package dev.lova.client.feature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import dev.lova.client.gui.WaypointEditScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaypointFeature {

    public static class Waypoint {
        public String name = "";
        public double x;
        public double y;
        public double z;
        public String dim = "";
        public int rgb = 0xFFFFFF;
    }

    public static class WorldData {
        public List<Waypoint> points = new ArrayList<>();
        public Waypoint death = null;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, WorldData> data = new HashMap<>();
    private static KeyMapping key;
    private static boolean wasDown = false;
    private static boolean wasDead = false;

    public static void init() {
        load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "waypoint"));
        key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.lovaclient.waypoint",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "waypoint"),
                WaypointFeature::render);
    }

    public static KeyMapping getKey() {
        return key;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("lovaclient_waypoints.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    Type t = new TypeToken<Map<String, WorldData>>() { }.getType();
                    Map<String, WorldData> m = GSON.fromJson(r, t);
                    if (m != null) {
                        data = m;
                    }
                }
            }
        } catch (Exception e) {
            LovaClient.LOGGER.error("Waypoints could not be loaded", e);
        }
        data.values().removeIf(wd -> wd == null);
        for (WorldData wd : data.values()) {
            if (wd.points == null) {
                wd.points = new ArrayList<>();
            }
            wd.points.removeIf(p -> p == null);
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(data));
        } catch (IOException e) {
            LovaClient.LOGGER.error("Waypoints could not be saved", e);
        }
    }

    private static String worldKey(Minecraft mc) {
        ServerData sd = mc.getCurrentServer();
        return sd != null ? "s:" + sd.ip : "single";
    }

    private static WorldData world() {
        return data.computeIfAbsent(worldKey(Minecraft.getInstance()), s -> new WorldData());
    }

    private static String dimOf(Minecraft mc) {
        return mc.level.dimension().toString();
    }

    public static List<Waypoint> points() {
        return world().points;
    }

    public static Waypoint death() {
        return world().death;
    }

    public static void clearDeath() {
        world().death = null;
        save();
    }

    private static boolean nameExists(String name) {
        for (Waypoint p : world().points) {
            if (name.equals(p.name)) {
                return true;
            }
        }
        return false;
    }

    /** Oyuncunun şu anki konumunda yeni nokta oluşturur (listeye eklemez). */
    public static Waypoint newPoint(Minecraft mc) {
        int n = world().points.size() + 1;
        while (nameExists("Nokta " + n)) {
            n++;
        }
        Waypoint wp = new Waypoint();
        wp.name = "Nokta " + n;
        wp.x = mc.player.getX();
        wp.y = mc.player.getY();
        wp.z = mc.player.getZ();
        wp.dim = dimOf(mc);
        wp.rgb = 0xFFFFFF;
        return wp;
    }

    private static void tick(Minecraft mc) {
        boolean down = key != null && key.isDown();
        if (down && !wasDown && mc.player != null && mc.level != null
                && mc.screen == null && LovaConfig.INSTANCE.waypointEnabled) {
            mc.setScreen(new WaypointEditScreen(null, newPoint(mc), true));
        }
        wasDown = down;

        if (mc.player == null || mc.level == null) {
            wasDead = false;
            return;
        }
        boolean dead = mc.player.getHealth() <= 0f;
        if (dead && !wasDead && LovaConfig.INSTANCE.waypointDeathEnabled) {
            Waypoint d = new Waypoint();
            d.name = "Son ölüm";
            d.x = mc.player.getX();
            d.y = mc.player.getY();
            d.z = mc.player.getZ();
            d.dim = dimOf(mc);
            d.rgb = 0xFF3030;
            world().death = d;
            save();
        }
        wasDead = dead;
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        LovaConfig cfg = LovaConfig.INSTANCE;
        if (!cfg.waypointEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        float scale = (float) cfg.waypointScale;
        if (scale < 0.05f) {
            return;
        }
        WorldData wd = world();
        if (wd.points.isEmpty() && (wd.death == null || !cfg.waypointDeathEnabled)) {
            return;
        }
        String dim = dimOf(mc);
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 cp = cam.position();
        Quaternionf inv = new Quaternionf(cam.rotation()).conjugate();
        float tanHalf = (float) Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        int w = g.guiWidth();
        int h = g.guiHeight();
        float aspect = (float) w / (float) h;

        for (Waypoint p : wd.points) {
            drawOne(g, mc, p, dim, cp, inv, tanHalf, w, h, aspect, scale);
        }
        if (cfg.waypointDeathEnabled && wd.death != null) {
            drawOne(g, mc, wd.death, dim, cp, inv, tanHalf, w, h, aspect, scale);
        }
    }

    private static void drawOne(GuiGraphics g, Minecraft mc, Waypoint p, String dim, Vec3 cp,
                                Quaternionf inv, float tanHalf, int w, int h, float aspect,
                                float scale) {
        if (p.dim == null || !p.dim.equals(dim)) {
            return;
        }
        Vector3f v = new Vector3f(
                (float) (p.x - cp.x),
                (float) (p.y + 1.0 - cp.y),
                (float) (p.z - cp.z));
        v.rotate(inv);
        float depth = -v.z;
        if (depth < 0.1f) {
            return;
        }
        float sx = (v.x / (depth * tanHalf * aspect) * 0.5f + 0.5f) * w;
        float sy = (0.5f - v.y / (depth * tanHalf) * 0.5f) * h;
        if (sx < 0 || sx > w || sy < 0 || sy > h) {
            return;
        }
        double dx = p.x - mc.player.getX();
        double dy = p.y - mc.player.getY();
        double dz = p.z - mc.player.getZ();
        long dist = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
        String name = p.name == null ? "" : p.name;
        String distText = dist + " m";
        int color = 0xFF000000 | (p.rgb & 0xFFFFFF);

        g.pose().pushMatrix();
        g.pose().translate(sx, sy);
        g.pose().scale(scale, scale);
        for (int i = -3; i <= 3; i++) {
            int half = 3 - Math.abs(i);
            g.fill(-half, i, half + 1, i + 1, color);
        }
        g.drawString(mc.font, name, -mc.font.width(name) / 2, -16, color, true);
        g.drawString(mc.font, distText, -mc.font.width(distText) / 2, 6, color, true);
        g.pose().popMatrix();
    }
}
