package net.nimrod.noted.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(at = @At("RETURN"), method = "tick()V", cancellable = true)
    private void tick(CallbackInfo ci) {
        Noted.INSTANCE.onTick();
    }   

}