package dev.lova.client.feature;

import dev.lova.client.LovaConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public class NoHurtCam {
    private static Double saved = null;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(NoHurtCam::tick);
    }

    private static void tick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<Double> opt = client.options.damageTiltStrength();
        if (LovaConfig.INSTANCE.noHurtCam) {
            if (saved == null) {
                saved = opt.get();
            }
            double target = Math.max(0.0, Math.min(1.0, LovaConfig.INSTANCE.hurtCamStrength));
            if (Math.abs(opt.get() - target) > 0.001) {
                opt.set(target);
            }
        } else if (saved != null) {
            opt.set(saved);
            saved = null;
        }
    }
}
