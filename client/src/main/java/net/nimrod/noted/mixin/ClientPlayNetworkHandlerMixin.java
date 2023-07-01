package net.nimrod.noted.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "Lnet/minecraft/network/listener/ClientPlayPacketListener;onDisconnect(Lnet/minecraft/network/packet/s2c/play/DisconnectS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
        Noted.INSTANCE.songPlayer.togglePaused();
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith(Noted.INSTANCE.commandManager.getPrefix())) {
            Noted.INSTANCE.commandManager.runCommand(message.substring(Noted.INSTANCE.commandManager.getPrefix().length()));
            ci.cancel();
        }
    }

}
