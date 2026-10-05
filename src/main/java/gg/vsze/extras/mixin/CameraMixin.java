package gg.vsze.extras.mixin;

import gg.vsze.extras.Freelook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: kamera memakai arah sendiri, bukan arah badan pemain. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void vsze$free(CallbackInfo ci) {
        if (Freelook.active) setRotation(Freelook.yaw, Freelook.pitch);
    }
}
