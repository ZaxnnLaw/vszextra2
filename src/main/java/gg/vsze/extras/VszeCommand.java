package gg.vsze.extras;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

/**
 * /vszeextra : pengaturan HUD lewat chat. Alias: /vszehud.
 *   /vszeextra list                     daftar modul + status + posisi
 *   /vszeextra set <modul> on|off       nyalakan / matikan modul
 *   /vszeextra toggle <modul>
 *   /vszeextra <modul> on|off           (bentuk singkat)
 *   /vszeextra pos <elemen> <x> <y>     posisi dalam persen layar (0-100), mis. 50 0 = tengah atas
 *   /vszeextra pos <elemen> default     kembali ke posisi bawaan
 *   /vszeextra bg <0-100>               opasitas latar panel
 *   /vszeextra warna <RRGGBB>           warna aksen (mis. FF7A3D)
 *   /vszeextra menu                     buka menu VSZE (sama seperti Right Shift)
 *   /vszeextra reset                    kembalikan semua pengaturan tampilan HUD
 */
public final class VszeCommand {
    private VszeCommand() {}

    private static final SuggestionProvider<FabricClientCommandSource> MODS = (ctx, b) -> {
        List<String> ids = new ArrayList<>();
        for (Modules.M m : Modules.ALL) ids.add(m.id());
        return CommandSource.suggestMatching(ids, b);
    };
    private static final SuggestionProvider<FabricClientCommandSource> ELS = (ctx, b) -> CommandSource.suggestMatching(java.util.Arrays.asList(HudCfg.ELEMENTS), b);

    public static LiteralArgumentBuilder<FabricClientCommandSource> root(String name) {
        return ClientCommandManager.literal(name)
            .executes(ctx -> help(ctx.getSource()))
            .then(ClientCommandManager.literal("list").executes(ctx -> list(ctx.getSource())))
            .then(ClientCommandManager.literal("menu").executes(ctx -> { ExtrasClient.pendingMenu = true; return 1; }))
            .then(ClientCommandManager.literal("reset").executes(ctx -> {
                HudCfg.reset();
                return say(ctx.getSource(), "Tampilan HUD dikembalikan ke bawaan (on/off modul tidak berubah).");
            }))
            .then(ClientCommandManager.literal("toggle")
                .then(ClientCommandManager.argument("modul", StringArgumentType.word()).suggests(MODS)
                    .executes(ctx -> toggle(ctx, "modul"))))
            .then(ClientCommandManager.literal("set")
                .then(ClientCommandManager.argument("modul", StringArgumentType.word()).suggests(MODS)
                    .then(ClientCommandManager.literal("on").executes(ctx -> set(ctx, "modul", true)))
                    .then(ClientCommandManager.literal("off").executes(ctx -> set(ctx, "modul", false)))))
            .then(ClientCommandManager.literal("pos")
                .then(ClientCommandManager.argument("elemen", StringArgumentType.word()).suggests(ELS)
                    .then(ClientCommandManager.literal("default").executes(ctx -> {
                        String el = StringArgumentType.getString(ctx, "elemen");
                        if (!HudCfg.known(el)) return unknownEl(ctx.getSource(), el);
                        HudCfg.clearPos(el);
                        return say(ctx.getSource(), el + ": posisi bawaan.");
                    }))
                    .then(ClientCommandManager.argument("x", FloatArgumentType.floatArg(0, 100))
                        .then(ClientCommandManager.argument("y", FloatArgumentType.floatArg(0, 100))
                            .executes(ctx -> {
                                String el = StringArgumentType.getString(ctx, "elemen");
                                if (!HudCfg.known(el)) return unknownEl(ctx.getSource(), el);
                                float x = FloatArgumentType.getFloat(ctx, "x"), y = FloatArgumentType.getFloat(ctx, "y");
                                HudCfg.setPos(el, x, y);
                                return say(ctx.getSource(), el + " dipindah ke " + fmt(x) + "% / " + fmt(y) + "%");
                            })))))
            .then(ClientCommandManager.literal("bg")
                .then(ClientCommandManager.argument("persen", IntegerArgumentType.integer(0, 100)).executes(ctx -> {
                    int v = IntegerArgumentType.getInteger(ctx, "persen");
                    HudCfg.setBg(v);
                    return say(ctx.getSource(), "Latar panel HUD: " + v + "%");
                })))
            .then(ClientCommandManager.literal("warna")
                .then(ClientCommandManager.argument("hex", StringArgumentType.word()).executes(ctx -> {
                    String h = StringArgumentType.getString(ctx, "hex").replace("#", "");
                    try {
                        if (h.length() != 6) throw new NumberFormatException();
                        HudCfg.setAccent(Integer.parseInt(h, 16));
                        return say(ctx.getSource(), "Warna aksen HUD: #" + h.toUpperCase());
                    } catch (NumberFormatException e) {
                        return say(ctx.getSource(), "Format warna: 6 digit hex, contoh FF7A3D");
                    }
                })))
            // bentuk singkat, kompatibel dengan /vszehud <modul> on|off
            .then(ClientCommandManager.argument("m", StringArgumentType.word()).suggests(MODS)
                .then(ClientCommandManager.literal("on").executes(ctx -> set(ctx, "m", true)))
                .then(ClientCommandManager.literal("off").executes(ctx -> set(ctx, "m", false))));
    }

    private static int say(FabricClientCommandSource s, String msg) {
        s.sendFeedback(Text.literal("[VSZE] " + msg));
        return 1;
    }

    private static String fmt(float v) { return v == Math.rint(v) ? String.valueOf((int) v) : String.format("%.1f", v); }

    private static int unknownEl(FabricClientCommandSource s, String el) {
        return say(s, "Elemen '" + el + "' tidak ada. Pilihan: " + String.join(", ", HudCfg.ELEMENTS));
    }

    private static int set(CommandContext<FabricClientCommandSource> ctx, String arg, boolean v) {
        String id = StringArgumentType.getString(ctx, arg);
        if (!Modules.known(id)) return say(ctx.getSource(), "Modul '" + id + "' tidak ada. Ketik /vszeextra list.");
        Modules.set(id, v);
        return say(ctx.getSource(), id + ": " + (v ? "ON" : "OFF"));
    }

    private static int toggle(CommandContext<FabricClientCommandSource> ctx, String arg) {
        String id = StringArgumentType.getString(ctx, arg);
        if (!Modules.known(id)) return say(ctx.getSource(), "Modul '" + id + "' tidak ada. Ketik /vszeextra list.");
        Modules.toggle(id);
        return say(ctx.getSource(), id + ": " + (Modules.on(id) ? "ON" : "OFF"));
    }

    private static int list(FabricClientCommandSource s) {
        say(s, "Modul:");
        for (Modules.M m : Modules.ALL) s.sendFeedback(Text.literal("  " + m.id() + " - " + m.label() + ": " + (Modules.on(m.id()) ? "ON" : "OFF")));
        say(s, "Posisi HUD (persen layar):");
        for (String el : HudCfg.ELEMENTS) {
            float[] p = HudCfg.pos(el);
            s.sendFeedback(Text.literal("  " + el + ": " + (p == null ? "bawaan" : fmt(p[0]) + " / " + fmt(p[1]))));
        }
        s.sendFeedback(Text.literal("  latar " + HudCfg.bgPercent() + "%, warna #" + String.format("%06X", HudCfg.accentRgb())));
        return 1;
    }

    private static int help(FabricClientCommandSource s) {
        say(s, "Pengaturan HUD:");
        for (String l : new String[] {
            "/vszeextra list - daftar modul + posisi",
            "/vszeextra set <modul> on|off  (atau /vszeextra <modul> on|off)",
            "/vszeextra pos <elemen> <x> <y> - posisi dalam persen layar 0-100",
            "/vszeextra pos <elemen> default - posisi bawaan",
            "  elemen: info, keys, potion, armor, pvp",
            "/vszeextra bg <0-100> - opasitas latar panel",
            "/vszeextra warna <RRGGBB> - warna aksen",
            "/vszeextra menu - buka menu VSZE (Right Shift)",
            "/vszeextra reset - tampilan HUD ke bawaan"
        }) s.sendFeedback(Text.literal(l));
        return 1;
    }
}
