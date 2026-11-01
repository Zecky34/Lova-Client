package dev.lova.client.feature;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lova.client.LovaClient;
import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ZoomFeature {

    private static KeyMapping key;
    private static float current = 1.0f;
    private static long lastNano = 0;

    public static void init() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(LovaClient.MOD_ID, "main"));
        key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.lovaclient.zoom",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category));
    }

    public static KeyMapping getKey() {
        return key;
    }

    /** FOV çarpanı: 1.0 = zoom yok, küçüldükçe yakınlaşır. */
    public static float multiplier() {
        LovaConfig cfg = LovaConfig.INSTANCE;
        Minecraft mc = Minecraft.getInstance();
        float target = 1.0f;
        if (cfg.zoomEnabled && key != null && key.isDown() && mc.screen == null) {
            target = (float) Math.max(0.05, Math.min(1.0, cfg.zoomFov));
        }
        if (!cfg.zoomSmooth) {
            current = target;
            return current;
        }
        long now = System.nanoTime();
        float dt = lastNano == 0 ? 0f : (now - lastNano) / 1.0e9f;
        lastNano = now;
        if (dt > 0.25f) {
            dt = 0.25f;
        }
        current += (target - current) * (1.0f - (float) Math.exp(-dt * 14.0));
        if (Math.abs(target - current) < 0.001f) {
            current = target;
        }
        return current;
    }
}
