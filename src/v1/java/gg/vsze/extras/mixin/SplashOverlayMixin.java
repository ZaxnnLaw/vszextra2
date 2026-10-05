package gg.vsze.extras.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loading screen saat game dibuka: logo Mojang diganti gambar VSZE/ZANN layar penuh.
 * Progress bar bawaan game tetap digambar di atasnya. Kalau gambar gagal dimuat, logo asli tetap tampil.
 */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    private static final String DRAW = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V";
    private static final Identifier VSZE_BG = Identifier.of("vsze-extras", "loading_bg_dynamic");
    private static boolean tried, ok;
    private static int imgW, imgH;

    private static void load() {
        tried = true;
        try {
            Path p = FabricLoader.getInstance().getModContainer("vsze-extras").orElseThrow()
                    .findPath("assets/vsze-extras/textures/gui/loading_bg.png").orElseThrow();
            try (InputStream in = Files.newInputStream(p)) {
                NativeImage img = NativeImage.read(in);
                imgW = img.getWidth();
                imgH = img.getHeight();
                MinecraftClient.getInstance().getTextureManager().registerTexture(VSZE_BG, new NativeImageBackedTexture(img));
                ok = true;
            }
        } catch (Throwable t) {
            ok = false;
        }
    }

    /** Separuh kiri logo -> ganti dengan gambar layar penuh (cover). */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = DRAW, ordinal = 0), require = 0)
    private void vsze$bg(DrawContext ctx, Identifier tex, int x, int y, int w, int h, float u, float v, int rw, int rh, int tw, int th) {
        if (!tried) load();
        if (!ok) { ctx.drawTexture(tex, x, y, w, h, u, v, rw, rh, tw, th); return; }
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        float scale = Math.max((float) sw / imgW, (float) sh / imgH);
        int dw = Math.round(imgW * scale), dh = Math.round(imgH * scale);
        RenderSystem.defaultBlendFunc(); // alpha (fade) sudah di-set game lewat shader color
        ctx.drawTexture(VSZE_BG, (sw - dw) / 2, (sh - dh) / 2, dw, dh, 0f, 0f, imgW, imgH, imgW, imgH);
        RenderSystem.blendFunc(770, 1); // kembalikan seperti bawaan game
    }

    /** Separuh kanan logo -> dibuang kalau gambar VSZE aktif. */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = DRAW, ordinal = 1), require = 0)
    private void vsze$skip(DrawContext ctx, Identifier tex, int x, int y, int w, int h, float u, float v, int rw, int rh, int tw, int th) {
        if (!ok) ctx.drawTexture(tex, x, y, w, h, u, v, rw, rh, tw, th);
    }
}
