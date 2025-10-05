package nimrod.noted.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;
    @Shadow
    public ClientWorld world;

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void onTick(CallbackInfo ci) {
        if (player != null && world != null) {
            Noted.INSTANCE.onTick();
        }
    }
}
