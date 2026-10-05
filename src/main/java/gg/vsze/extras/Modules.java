package gg.vsze.extras;

import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

/** Daftar modul VSZE (on/off disimpan di ~/.vsze/modules.json) + logika per-tick yang ringan. */
public final class Modules {
    public record M(String id, String label) {}
    public static final M[] ALL = Compat.BLUR ? new M[] {
        new M("armor", "ArmorHUD"), new M("potion", "PotionHUD"), new M("fps", "FPS"), new M("cps", "CPS"),
        new M("coords", "Koordinat"), new M("ping", "Ping"), new M("server", "Server IP"), new M("keys", "Keystrokes"),
        new M("sprint", "Auto Sprint"), new M("zoom", "Zoom (tahan C)"), new M("freelook", "Freelook (tahan Alt)"),
        new M("blur", "Motion Blur (eksperimen)"), new M("badge", "Badge VSZE"), new M("notify", "Notif chat"),
        new M("reach", "Reach display"), new M("items", "Item counter PvP")
    } : new M[] { // Motion Blur hanya ada di 1.21 - 1.21.1 (sistem post-shader berubah di 1.21.2+)
        new M("armor", "ArmorHUD"), new M("potion", "PotionHUD"), new M("fps", "FPS"), new M("cps", "CPS"),
        new M("coords", "Koordinat"), new M("ping", "Ping"), new M("server", "Server IP"), new M("keys", "Keystrokes"),
        new M("sprint", "Auto Sprint"), new M("zoom", "Zoom (tahan C)"), new M("freelook", "Freelook (tahan Alt)"),
        new M("badge", "Badge VSZE"), new M("notify", "Notif chat"),
        new M("reach", "Reach display"), new M("items", "Item counter PvP")
    };
    private static final Set<String> DEFAULT_ON = Set.of("armor", "potion", "fps", "cps", "coords", "badge", "notify");
    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".vsze", "modules.json");
    private static final Map<String, Boolean> ON = new HashMap<>();

    public static KeyBinding zoomKey, lookKey;
    public static int cps;
    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown, zooming;
    private static int oldFov, blurCheck;

    static {
        try {
            @SuppressWarnings("unchecked") Map<String, Boolean> m = new Gson().fromJson(Files.readString(FILE), Map.class);
            if (m != null) ON.putAll(m);
        } catch (Exception ignored) {}
        sync();
    }

    public static boolean on(String id) { return ON.getOrDefault(id, DEFAULT_ON.contains(id)); }

    public static boolean known(String id) {
        for (M m : ALL) if (m.id().equals(id)) return true;
        return false;
    }

    public static void toggle(String id) { set(id, !on(id)); }

    public static void set(String id, boolean v) {
        ON.put(id, v);
        sync();
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, new Gson().toJson(ON)); } catch (Exception ignored) {}
    }

    private static void sync() { Presence.enabled = on("badge"); Presence.notify = on("notify"); }

    /** Dipanggil tiap tick client. */
    public static void tick(MinecraftClient c) {
        if (c.player == null) { Freelook.active = false; return; }
        // CPS: polling tombol kiri mouse (tanpa mixin)
        boolean down = c.currentScreen == null && org.lwjgl.glfw.GLFW.glfwGetMouseButton(c.getWindow().getHandle(), 0) == 1;
        long now = System.currentTimeMillis();
        if (down && !wasDown) clicks.add(now);
        wasDown = down;
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) clicks.pollFirst();
        cps = clicks.size();
        // Auto sprint
        if (on("sprint") && c.options.forwardKey.isPressed() && !c.player.isSneaking() && !c.player.horizontalCollision
                && c.player.getHungerManager().getFoodLevel() > 6) c.player.setSprinting(true);
        // Zoom: tahan C
        boolean z = on("zoom") && zoomKey != null && zoomKey.isPressed() && c.currentScreen == null;
        if (z && !zooming) { oldFov = c.options.getFov().getValue(); zooming = true; }
        if (zooming) { c.options.getFov().setValue(z ? 30 : oldFov); if (!z) zooming = false; }
        // Freelook: tahan Alt, kamera bebas tanpa memutar badan
        boolean f = on("freelook") && lookKey != null && lookKey.isPressed() && c.currentScreen == null;
        if (f && !Freelook.active) { Freelook.yaw = c.player.getYaw(); Freelook.pitch = c.player.getPitch(); }
        Freelook.active = f;
        // Motion blur (hanya 1.21 - 1.21.1): dicek tiap 1 detik, gagal = diam saja
        if (Compat.BLUR && ++blurCheck >= 20) {
            blurCheck = 0;
            Compat.blur(c, on("blur"));
        }
    }

    private Modules() {}
}
