package gg.vsze.extras;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Background menu utama VSZE/ZANN (menggantikan panorama). Titik pemasangannya beda per versi: lihat mixin TitleBg* di src/vN. */
public final class TitleBg {
    private static final Identifier BG = Identifier.of("vsze-extras", "textures/gui/menu_bg.png");
    private static final int W = 1408, H = 768;
    /** true kalau gambar VSZE sudah tergambar di menu utama; logo MINECRAFT baru disembunyikan kalau true. */
    public static boolean drawn;

    private TitleBg() {}

    public static void draw(DrawContext ctx, int sw, int sh) {
        // cover: penuhi layar, gambar dipotong di tengah
        float scale = Math.max((float) sw / W, (float) sh / H);
        int dw = Math.round(W * scale), dh = Math.round(H * scale);
        Compat.blit(ctx, BG, (sw - dw) / 2, (sh - dh) / 2, dw, dh, W, H);
        // gelapkan sedikit bagian tengah ke bawah supaya tombol tetap terbaca
        ctx.fillGradient(0, sh / 4, sw, sh, 0x00000000, 0xB0000000);
        drawn = true;
    }
}
