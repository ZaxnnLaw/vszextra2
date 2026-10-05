package gg.vsze.extras;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * /vsze about   : baris 1 = ikon VSZE, baris 2 (di bawah ikon) = teks about.
 * /vszehack on|off : cuma BERCANDA. Tidak mengubah apa pun di game; efeknya hanya teks di bawah ikon pada /vsze about.
 */
public final class VszeAbout {
    private VszeAbout() {}

    /** Flag bercanda, tidak dipakai di tempat lain dan tidak menyentuh gameplay. */
    public static volatile boolean joke;

    public static LiteralArgumentBuilder<FabricClientCommandSource> root() {
        return ClientCommandManager.literal("vsze")
            .executes(ctx -> about(ctx.getSource()))
            .then(ClientCommandManager.literal("about").executes(ctx -> about(ctx.getSource())));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> hackRoot() {
        return ClientCommandManager.literal("vszehack")
            .then(ClientCommandManager.literal("on").executes(ctx -> flag(ctx.getSource(), true)))
            .then(ClientCommandManager.literal("off").executes(ctx -> flag(ctx.getSource(), false)));
    }

    private static int flag(FabricClientCommandSource s, boolean v) {
        joke = v;
        s.sendFeedback(Text.literal("[VSZE] hack: " + (v ? "ON" : "OFF")));
        return 1;
    }

    private static String version() {
        try {
            return FabricLoader.getInstance().getModContainer("vsze-extras")
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?");
        } catch (Throwable t) { return "?"; }
    }

    private static int about(FabricClientCommandSource s) {
        s.sendFeedback(Presence.iconText());
        if (joke) s.sendFeedback(Text.literal("hack client premium vsze active").formatted(Formatting.GOLD));
        else s.sendFeedback(Text.literal("VSZE Client by ZANN - VSZE Extras " + version()).formatted(Formatting.GRAY));
        return 1;
    }
}
