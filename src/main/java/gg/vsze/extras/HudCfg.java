package gg.vsze.extras;

import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;

/**
 * Pengaturan tampilan HUD, disimpan di ~/.vsze/hud.json.
 * Posisi dalam persen ruang kosong layar (0-100); tidak diatur = posisi bawaan.
 * Dengan persen, HUD tidak pernah keluar layar dan tetap benar di GUI scale berapa pun.
 */
public final class HudCfg {
    public static final String[] ELEMENTS = {"info", "keys", "potion", "armor", "pvp"};
    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".vsze", "hud.json");

    private static final class Data {
        Map<String, float[]> pos = new HashMap<>();
        int bg = 56;            // opasitas latar panel, 0-100
        int accent = 0xFF7A3D;  // warna aksen RGB
    }

    private static Data d = new Data();

    static {
        try {
            Data x = new Gson().fromJson(Files.readString(FILE), Data.class);
            if (x != null) { d = x; if (d.pos == null) d.pos = new HashMap<>(); }
        } catch (Exception ignored) {}
    }

    private HudCfg() {}

    public static boolean known(String id) { return Arrays.asList(ELEMENTS).contains(id); }
    public static boolean custom(String id) { return d.pos.containsKey(id); }
    public static float[] pos(String id) { return d.pos.get(id); }
    public static int bgPercent() { return d.bg; }
    public static int accentRgb() { return d.accent; }
    public static int accent() { return 0xFF000000 | d.accent; }
    public static int accentDown() { return 0xC0000000 | d.accent; }
    public static int bgColor() { return (Math.round(d.bg * 2.55f) << 24) | 0x101216; }

    /** Posisi X piksel: persen dari ruang kosong, atau def kalau belum diatur. */
    public static int x(String id, int def, int screenW, int elemW) {
        float[] p = d.pos.get(id);
        return p == null ? def : clamp(Math.round(p[0] / 100f * (screenW - elemW)), 0, Math.max(0, screenW - elemW));
    }

    public static int y(String id, int def, int screenH, int elemH) {
        float[] p = d.pos.get(id);
        return p == null ? def : clamp(Math.round(p[1] / 100f * (screenH - elemH)), 0, Math.max(0, screenH - elemH));
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    public static void setPos(String id, float x, float y) { d.pos.put(id, new float[] {x, y}); save(); }
    public static void clearPos(String id) { d.pos.remove(id); save(); }
    public static void setBg(int percent) { d.bg = clamp(percent, 0, 100); save(); }
    public static void setAccent(int rgb) { d.accent = rgb & 0xFFFFFF; save(); }
    public static void reset() { d = new Data(); save(); }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, new Gson().toJson(d));
        } catch (Exception ignored) {}
    }
}
