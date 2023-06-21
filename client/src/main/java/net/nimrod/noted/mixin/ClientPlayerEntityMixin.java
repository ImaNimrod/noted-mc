package net.nimrod.noted.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable=true)
    private void onChatMessage(String message, Text text, CallbackInfo ci) {
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

    @Inject(at = @At("RETURN"), method = "tick()V", cancellable = true)
    private void tick(CallbackInfo ci) {
        Noted.INSTANCE.onTick();
    }   

}
