package gg.vsze.extras;

import java.util.UUID;
import gg.vsze.extras.mixin.GameRendererAccess;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

/** Bagian yang API-nya berbeda antar versi Minecraft. Varian untuk 1.21 - 1.21.1. */
public final class Compat {
    public static final boolean BLUR = true;
    private static boolean blurLoaded;

    private Compat() {}

    public static KeyBinding keybind(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, "VSZE"));
    }

    public static UUID uuid(PlayerListEntry e) { return e.getProfile().getId(); }

    /** Gambar seluruh tekstur (tw x th) ke kotak x,y,w,h. */
    public static void blit(DrawContext g, Identifier tex, int x, int y, int w, int h, int tw, int th) {
        g.drawTexture(tex, x, y, w, h, 0f, 0f, tw, th, tw, th);
    }

    /** Motion blur memakai post-shader "phosphor" milik vanilla. Gagal = diam saja. */
    public static void blur(MinecraftClient c, boolean want) {
        try {
            var gr = c.gameRenderer;
            if (want && (!blurLoaded || ((GameRendererAccess) gr).vsze$post() == null)) {
                ((GameRendererAccess) gr).vsze$load(Identifier.of("vsze-extras", "shaders/post/motion_blur.json"));
                blurLoaded = true;
            } else if (!want && blurLoaded) { gr.disablePostProcessor(); blurLoaded = false; }
        } catch (Throwable ignored) { blurLoaded = false; }
    }
}
