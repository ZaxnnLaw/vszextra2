package gg.vsze.extras.mixin;

import gg.vsze.extras.Presence;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Nametag di atas kepala (dan nama di pesan sistem) diberi ikon untuk pengguna launcher. */
@Mixin(PlayerEntity.class)
public abstract class PlayerNameMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void vsze$badge(CallbackInfoReturnable<Text> cir) {
        PlayerEntity p = (PlayerEntity) (Object) this;
        if (p instanceof AbstractClientPlayerEntity && Presence.enabled && Presence.isUser(p.getUuid()))
            cir.setReturnValue(Presence.badge(cir.getReturnValue()));
    }
}
