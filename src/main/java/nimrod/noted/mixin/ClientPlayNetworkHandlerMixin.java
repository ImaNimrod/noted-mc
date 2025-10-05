package nimrod.noted.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import nimrod.noted.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nimrod.noted.Noted.MC;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        String prefix = "@"; // TODO: make an actual config variable
        if (message.startsWith(prefix)) {
            try {
                CommandManager.dispatch(message.substring(prefix.length()));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }

            MC.inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }
    }
}
