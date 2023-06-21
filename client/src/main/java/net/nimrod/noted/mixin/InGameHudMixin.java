package net.nimrod.noted.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.nimrod.noted.Noted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrixStack, float tickDelta, CallbackInfo ci) {
        Noted.INSTANCE.onHudRender(matrixStack, tickDelta);
    }

}
