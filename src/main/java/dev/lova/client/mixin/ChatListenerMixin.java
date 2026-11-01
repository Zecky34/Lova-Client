package dev.lova.client.mixin;

import dev.lova.client.feature.ChatTimestampFeature;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {

    private static boolean lovaclient$stamping = false;

    @Inject(method = "handleDisguisedChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"), cancellable = true)
    private void lovaclient$stamp(Component message, ChatType.Bound bound, CallbackInfo ci) {
        if (lovaclient$stamping) {
            return;
        }
        Component stamped = ChatTimestampFeature.stamp(message);
        if (stamped == message) {
            return;
        }
        lovaclient$stamping = true;
        try {
            ((ChatListener) (Object) this).handleDisguisedChatMessage(stamped, bound);
        } finally {
            lovaclient$stamping = false;
        }
        ci.cancel();
    }
}
