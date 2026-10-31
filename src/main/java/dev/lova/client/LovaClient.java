package dev.lova.client;

import dev.lova.client.feature.CrosshairFeature;
import dev.lova.client.feature.DamageTint;
import dev.lova.client.feature.HudFeature;
import dev.lova.client.feature.NoHurtCam;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LovaClient implements ClientModInitializer {
    public static final String MOD_ID = "lovaclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LovaConfig.load();
        NoHurtCam.init();
        CrosshairFeature.init();
        DamageTint.init();
        HudFeature.init();
        LOGGER.info("Lova Client loaded");
    }
}
