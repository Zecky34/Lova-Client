package dev.lova.client.mixin;

import dev.lova.client.feature.DamageTint;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin {
    @Shadow
    @Final
    private DynamicTexture texture;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lova$onInit(CallbackInfo ci) {
        DamageTint.register(this.texture);
    }
}
