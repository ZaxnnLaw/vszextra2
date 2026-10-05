package gg.vsze.extras.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Akses ke post-processor GameRenderer untuk Motion Blur (1.21 - 1.21.1). Harus di package mixin supaya terdaftar. */
@Mixin(GameRenderer.class)
public interface GameRendererAccess {
    @Invoker("loadPostProcessor") void vsze$load(Identifier id);
    @Accessor("postProcessor") PostEffectProcessor vsze$post();
}
