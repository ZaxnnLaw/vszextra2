package gg.vsze.extras.mixin;

import gg.vsze.extras.TitleBg;
import net.minecraft.client.gui.LogoDrawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Logo MINECRAFT bawaan disembunyikan karena background sudah berisi logo VSZE/ZANN. */
@Mixin(LogoDrawer.class)
public abstract class LogoDrawerMixin {
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void vsze$hideLogo(CallbackInfo ci) {
        if (TitleBg.drawn) ci.cancel(); // hanya kalau background VSZE benar-benar tergambar
    }
}
