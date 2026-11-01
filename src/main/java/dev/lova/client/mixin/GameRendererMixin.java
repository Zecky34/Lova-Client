package dev.lova.client.mixin;

import dev.lova.client.feature.ZoomFeature;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void lovaclient$zoom(Camera camera, float partialTick, boolean useFovSetting,
                                 CallbackInfoReturnable<Float> cir) {
        float m = ZoomFeature.multiplier();
        if (m < 0.9999f) {
            cir.setReturnValue(cir.getReturnValue() * m);
        }
    }
}
