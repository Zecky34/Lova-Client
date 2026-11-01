package dev.lova.client.mixin;

import dev.lova.client.feature.ChatTimestampFeature;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    private static boolean lovaclient$stamping = false;

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void lovaclient$stamp(Component message, MessageSignature signature, GuiMessageTag tag,
                                  CallbackInfo ci) {
        if (lovaclient$stamping) {
            return;
        }
        if (message.getString().matches("(?s).*\\d\\d:\\d\\d$")) {
            return;
        }
        Component stamped = ChatTimestampFeature.stamp(message);
        if (stamped == message) {
            return;
        }
        lovaclient$stamping = true;
        try {
            ((ChatComponent) (Object) this).addMessage(stamped, signature, tag);
        } finally {
            lovaclient$stamping = false;
        }
        ci.cancel();
    }
}
