package gg.vsze.extras.mixin;

import gg.vsze.extras.VszeScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tombol "VSZE" di pojok kiri atas menu ESC. */
@Mixin(GameMenuScreen.class)
public abstract class PauseMixin extends Screen {
    protected PauseMixin(Text t) { super(t); }

    @Inject(method = "init", at = @At("TAIL"))
    private void vsze$button(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("VSZE"), b -> this.client.setScreen(new VszeScreen((Screen) (Object) this))).dimensions(6, 6, 60, 20).build());
    }
}
