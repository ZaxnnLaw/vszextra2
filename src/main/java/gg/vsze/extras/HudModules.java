package gg.vsze.extras;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;

/**
 * HUD ringan: panel info (FPS/CPS/Koordinat/Ping/Server), Keystrokes, PotionHUD, dan panel PvP (Reach + Item counter).
 * Teks dihitung tiap 5 tick (bukan tiap frame) dan disimpan di cache, jadi render per frame hampir tanpa alokasi.
 * Posisi/latar/warna diatur lewat /vszeextra (lihat HudCfg).
 */
public final class HudModules {
    private HudModules() {}

    private static final List<String> info = new ArrayList<>(5), pot = new ArrayList<>(4), pvp = new ArrayList<>(5);
    private static final List<Integer> potCol = new ArrayList<>(4);
    private static int infoW, potW, pvpW, n;
    private static boolean wasAtk;
    private static double reach;
    private static long reachAt;

    /** Dipanggil tiap tick client (dari ExtrasClient). */
    public static void tick(MinecraftClient c) {
        if (c.player == null) return;
        if (Modules.on("reach")) {
            boolean atk = c.currentScreen == null && c.options.attackKey.isPressed();
            if (atk && !wasAtk && c.crosshairTarget instanceof EntityHitResult eh) {
                reach = c.player.getEyePos().distanceTo(eh.getPos());
                reachAt = System.currentTimeMillis();
            }
            wasAtk = atk;
        }
        if (++n < 5) return;
        n = 0;
        refresh(c);
    }

    private static void refresh(MinecraftClient c) {
        TextRenderer tr = c.textRenderer;
        info.clear(); pot.clear(); potCol.clear(); pvp.clear();

        if (Modules.on("fps")) info.add("FPS  " + c.getCurrentFps());
        if (Modules.on("cps")) info.add("CPS  " + Modules.cps);
        if (Modules.on("coords")) info.add(String.format("XYZ  %.0f %.0f %.0f", c.player.getX(), c.player.getY(), c.player.getZ()));
        if (Modules.on("ping") && c.getNetworkHandler() != null) {
            var e = c.getNetworkHandler().getPlayerListEntry(c.player.getUuid());
            info.add("Ping  " + (e == null ? 0 : e.getLatency()) + " ms");
        }
        if (Modules.on("server")) {
            var se = c.getCurrentServerEntry();
            info.add("IP  " + (se != null ? se.address : c.isInSingleplayer() ? "Singleplayer" : "-"));
        }
        infoW = widest(tr, info);

        if (Modules.on("reach")) {
            boolean fresh = System.currentTimeMillis() - reachAt < 4000;
            pvp.add("Reach  " + (fresh ? String.format("%.2f", reach) : "-"));
        }
        if (Modules.on("items")) {
            int totem = 0, pots = 0, gapple = 0, crystal = 0;
            PlayerInventory inv = c.player.getInventory();
            for (int i = 0, sz = inv.size(); i < sz; i++) {
                ItemStack s = inv.getStack(i);
                if (s.isEmpty()) continue;
                if (s.isOf(Items.TOTEM_OF_UNDYING)) totem += s.getCount();
                else if (s.isOf(Items.SPLASH_POTION)) pots += s.getCount();
                else if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) gapple += s.getCount();
                else if (s.isOf(Items.END_CRYSTAL)) crystal += s.getCount();
            }
            pvp.add("Totem  " + totem);
            pvp.add("Pot  " + pots);
            pvp.add("Gapple  " + gapple);
            pvp.add("Crystal  " + crystal);
        }
        pvpW = widest(tr, pvp);

        if (Modules.on("potion")) {
            for (StatusEffectInstance e : c.player.getStatusEffects()) {
                String time = e.isInfinite() ? "\u221e" : String.format("%d:%02d", e.getDuration() / 1200, e.getDuration() / 20 % 60);
                pot.add(e.getEffectType().value().getName().getString() + (e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "") + "  " + time);
                potCol.add(0xFF000000 | e.getEffectType().value().getColor());
            }
        }
        potW = widest(tr, pot);
    }

    private static int widest(TextRenderer tr, List<String> l) {
        int w = 0;
        for (String s : l) w = Math.max(w, tr.getWidth(s));
        return w;
    }

    public static void render(DrawContext g, RenderTickCounter t) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.options.hudHidden || c.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = c.textRenderer;
        int sw = c.getWindow().getScaledWidth(), sh = c.getWindow().getScaledHeight();

        int infoH = panel(g, tr, "info", info, infoW, 4, 4, sw, sh);
        panel(g, tr, "pvp", pvp, pvpW, 4, infoH > 0 ? 4 + infoH + 4 : 4, sw, sh);
        if (Modules.on("keys")) keys(g, c, tr, sw, sh);
        if (Modules.on("potion")) potions(g, tr, sw, sh);
    }

    /** Menggambar panel teks; mengembalikan tinggi panel (0 kalau kosong). */
    private static int panel(DrawContext g, TextRenderer tr, String id, List<String> ls, int textW, int defX, int defY, int sw, int sh) {
        if (ls.isEmpty()) return 0;
        int w = textW + 12, h = ls.size() * 11 + 6;
        int x = HudCfg.x(id, defX, sw, w), y = HudCfg.y(id, defY, sh, h);
        if (HudCfg.bgPercent() > 0) g.fill(x, y, x + w, y + h, HudCfg.bgColor());
        g.fill(x, y, x + 2, y + h, HudCfg.accent());
        int ty = y + 3;
        for (int i = 0, k = ls.size(); i < k; i++) { g.drawText(tr, ls.get(i), x + 6, ty, 0xFFFFFFFF, true); ty += 11; }
        return h;
    }

    private static void key(DrawContext g, TextRenderer tr, int x, int y, int w, String s, boolean down) {
        g.fill(x, y, x + w, y + 18, down ? HudCfg.accentDown() : HudCfg.bgColor());
        g.drawText(tr, s, x + (w - tr.getWidth(s)) / 2, y + 5, 0xFFFFFFFF, false);
    }

    private static void keys(DrawContext g, MinecraftClient c, TextRenderer tr, int sw, int sh) {
        var o = c.options;
        int x = HudCfg.x("keys", 6, sw, 58), y = HudCfg.y("keys", sh - 84, sh, 58);
        key(g, tr, x + 20, y, 18, "W", o.forwardKey.isPressed());
        key(g, tr, x, y + 20, 18, "A", o.leftKey.isPressed());
        key(g, tr, x + 20, y + 20, 18, "S", o.backKey.isPressed());
        key(g, tr, x + 40, y + 20, 18, "D", o.rightKey.isPressed());
        key(g, tr, x, y + 40, 28, "LMB", o.attackKey.isPressed());
        key(g, tr, x + 30, y + 40, 28, "RMB", o.useKey.isPressed());
    }

    private static void potions(DrawContext g, TextRenderer tr, int sw, int sh) {
        if (pot.isEmpty()) return;
        boolean custom = HudCfg.custom("potion");
        int h = pot.size() * 11;
        int x0 = HudCfg.x("potion", 0, sw, potW), y = HudCfg.y("potion", 24, sh, h);
        for (int i = 0, k = pot.size(); i < k; i++) {
            String s = pot.get(i);
            int x = custom ? x0 : sw - tr.getWidth(s) - 6; // bawaan rata kanan, kalau diposisikan manual rata kiri
            g.drawText(tr, s, x, y, potCol.get(i), true);
            y += 11;
        }
    }
}
