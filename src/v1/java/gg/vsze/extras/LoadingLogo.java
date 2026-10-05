package gg.vsze.extras;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.InputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/** Menggambar logo VSZE/ZANN Client + "Loading" menimpa logo Mojang Studios. */
public final class LoadingLogo {
    private static boolean tried;
    private static Identifier logo;
    private static int lw, lh;

    private LoadingLogo() {}

    private static void ensure() {
        if (tried) return;
        tried = true;
        try (InputStream in = LoadingLogo.class.getResourceAsStream("/assets/vsze-extras/logo.png")) {
            if (in == null) return;
            NativeImage img = NativeImage.read(in);
            lw = img.getWidth();
            lh = img.getHeight();
            Identifier id = Identifier.of("vsze_extras", "logo");
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
            logo = id;
        } catch (Throwable ignored) {
        }
    }

    /** alpha 0..1 (1 = penuh), progress 0..1 */
    public static void draw(DrawContext ctx, float alpha, float progress) {
        ensure();
        int ai = (int) (Math.max(0f, Math.min(1f, alpha)) * 255f);
        if (ai < 8) return;
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        MinecraftClient mc = MinecraftClient.getInstance();

        ctx.fill(0, 0, w, h, (ai << 24) | 0x0B0F1A);

        int cx = w / 2;
        int imgH = Math.min((int) (h * 0.42), 150);
        int imgW = logo != null && lh > 0 ? imgH * lw / lh : 0;
        int top = h / 2 - imgH / 2 - 14;

        if (logo != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            ctx.drawTexture(logo, cx - imgW / 2, top, imgW, imgH, 0f, 0f, lw, lh, lw, lh);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        } else {
            ctx.drawCenteredTextWithShadow(mc.textRenderer, "VSZE/ZANN", cx, top + imgH / 2 - 4, (ai << 24) | 0xFFFFFF);
        }

        int ty = top + imgH + 6;
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "Client", cx, ty, (ai << 24) | 0xFF8C00);
        int dots = (int) ((Util.getMeasuringTimeMs() / 400L) % 4L);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "Loading" + ".".repeat(dots), cx, ty + 14, (ai << 24) | 0xC8CED8);

        int bw = 140, bx = cx - bw / 2, by = ty + 30;
        ctx.fill(bx, by, bx + bw, by + 3, ((ai / 4) << 24) | 0xFFFFFF);
        int fw = (int) (bw * Math.max(0f, Math.min(1f, progress)));
        ctx.fill(bx, by, bx + fw, by + 3, (ai << 24) | 0xFF8C00);
    }
}
