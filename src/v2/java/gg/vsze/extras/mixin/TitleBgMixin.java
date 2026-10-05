package gg.vsze.extras.mixin;

import gg.vsze.extras.TitleBg;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.2+: panorama digambar lewat renderPanoramaBackground. Untuk TitleScreen, panorama diganti gambar VSZE/ZANN.
 * Kalau method itu tidak ada di versi ini, injector dilewati dan menu utama tetap vanilla (logo tidak disembunyikan).
 */
@Mixin({Screen.class, TitleScreen.class})
public abstract class TitleBgMixin {
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void vsze$background(DrawContext ctx, float delta, CallbackInfo ci) {
        if ((Object) this instanceof TitleScreen) {
            var w = MinecraftClient.getInstance().getWindow();
            TitleBg.draw(ctx, w.getScaledWidth(), w.getScaledHeight());
            ci.cancel();
        }
    }
}
