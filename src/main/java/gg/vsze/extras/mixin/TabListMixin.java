package gg.vsze.extras.mixin;

import gg.vsze.extras.Compat;
import gg.vsze.extras.Presence;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ikon juga muncul di tab list (Tab), termasuk untuk diri sendiri. */
@Mixin(PlayerListHud.class)
public class TabListMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void vsze$tab(PlayerListEntry e, CallbackInfoReturnable<Text> cir) {
        if (Presence.enabled && Presence.isUser(Compat.uuid(e)))
            cir.setReturnValue(Presence.badge(cir.getReturnValue()));
    }
}
