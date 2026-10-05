package gg.vsze.extras;

import java.util.UUID;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

/** Bagian yang API-nya berbeda antar versi Minecraft. Varian untuk 1.21.6 - 1.21.8. */
public final class Compat {
    /** Motion Blur tidak tersedia (sistem post-shader berubah di 1.21.2). */
    public static final boolean BLUR = false;

    private Compat() {}

    public static KeyBinding keybind(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, "VSZE"));
    }

    public static UUID uuid(PlayerListEntry e) { return e.getProfile().getId(); }

    public static void blit(DrawContext g, Identifier tex, int x, int y, int w, int h, int tw, int th) {
        g.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0f, 0f, w, h, tw, th, tw, th);
    }

    public static void blur(MinecraftClient c, boolean want) {}
}
