package net.nimrod.noted.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("%")) {
            if (message.substring(1).equalsIgnoreCase("activate")) {
                Noted.INSTANCE.active = true;
            } else if (message.substring(1).equalsIgnoreCase("stop")) {
                Noted.INSTANCE.active = false;
                Noted.INSTANCE.currentSong = null;
            }

            ci.cancel();
        }
    }

}