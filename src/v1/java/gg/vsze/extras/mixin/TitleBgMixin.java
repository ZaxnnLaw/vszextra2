package gg.vsze.extras.mixin;

import gg.vsze.extras.TitleBg;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.21 - 1.21.1: ganti panorama bawaan dengan gambar VSZE/ZANN (digambar setelah panorama, sebelum logo & tombol). */
@Mixin(TitleScreen.class)
public abstract class TitleBgMixin extends Screen {
    protected TitleBgMixin(Text title) { super(title); }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V"))
    private void vsze$background(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleBg.draw(ctx, this.width, this.height);
    }
}
