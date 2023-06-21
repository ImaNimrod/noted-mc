package net.nimrod.noted.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith("%")) {
            if (message.substring(1).equalsIgnoreCase("activate")) {
                Noted.INSTANCE.songPlayer.setActive(true);
            } else if (message.substring(1).equalsIgnoreCase("stop")) {
                Noted.INSTANCE.songPlayer.setActive(false);
                Noted.INSTANCE.songPlayer.reset();
            }

            ci.cancel();
        }
    }

}
