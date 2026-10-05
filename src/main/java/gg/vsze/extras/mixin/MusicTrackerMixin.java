package gg.vsze.extras.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Matikan musik menu bawaan Minecraft supaya tidak bentrok dengan musik VSZE. */
@Mixin(MusicTracker.class)
public abstract class MusicTrackerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void vsze$noMenuMusic(CallbackInfo ci) {
        if (this.client.world == null) ci.cancel();
    }
}
