package gg.vsze.extras;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;

/**
 * Badge "[icon] nama", status "di game + IP server", daftar teman (dari ~/.vsze/friends.json yang ditulis launcher) dan chat teman.
 * Semua HTTP jalan di thread terpisah; alamat server dari -Dvsze.presence=URL (diisi launcher).
 */
public final class Presence {
    public static boolean enabled = true, notify = true;
    /** Ikon badge: teks "\uE000" dengan font vsze-extras:badge (dibuat lewat codec Text supaya sama di semua versi). */
    private static final Text ICON = icon();
    private static Text icon() {
        try {
            JsonElement j = JsonParser.parseString("{\"text\":\"\\uE000\",\"font\":\"vsze-extras:badge\"}");
            return TextCodecs.CODEC.parse(JsonOps.INSTANCE, j).result().orElse(Text.literal(""));
        } catch (Throwable t) { return Text.literal(""); }
    }
    /** Ikon badge VSZE untuk dipakai di tempat lain (mis. /vsze about). */
    public static Text iconText() { return ICON; }
    public record Friend(String name, String id, String status, String server, String tier) {}
    public static volatile List<Friend> friends = List.of();
    public static final Map<String, List<String>> LOG = new ConcurrentHashMap<>();

    private static final String BASE = System.getProperty("vsze.presence", "").replaceAll("/+$", "");
    private static final Path FRIENDS = Paths.get(System.getProperty("user.home"), ".vsze", "friends.json");
    private static final Set<String> USERS = ConcurrentHashMap.newKeySet();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static int slow = 580, fast = 0;
    private static long since = -1;
    private static volatile boolean busy, busy2;

    private Presence() {}

    static String key(UUID u) { return u.toString().replace("-", ""); }
    /** Friends/chat/presence hanya untuk akun premium (Microsoft). */
    public static boolean premium() {
        // getAccountType() dihapus di Minecraft 1.21.6+, jadi pakai UUID: akun Microsoft = UUID v4, akun offline = v3.
        UUID id = MinecraftClient.getInstance().getSession().getUuidOrNull();
        return id != null && id.version() == 4;
    }
    public static boolean connected() { return !BASE.isEmpty(); }

    public static boolean isUser(UUID u) {
        if (u == null) return false;
        MinecraftClient c = MinecraftClient.getInstance();
        return (c.player != null && u.equals(c.player.getUuid())) || USERS.contains(key(u));
    }

    public static MutableText badge(Text name) {
        MutableText r = Text.empty();
        r.append(ICON);
        r.append(Text.literal(" "));
        r.append(name);
        return r;
    }

    public static void onJoin() { USERS.clear(); slow = 580; since = -1; }

    private static String get(String path) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString()).body();
    }

    private static void post(String path, JsonObject o) throws Exception {
        HTTP.send(HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(o.toString())).build(), HttpResponse.BodyHandlers.discarding());
    }

    public static void send(String toId, String text) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (BASE.isEmpty() || c.player == null) return;
        JsonObject o = new JsonObject();
        o.addProperty("from", key(c.player.getUuid())); o.addProperty("name", c.player.getName().getString());
        o.addProperty("to", toId); o.addProperty("text", text);
        LOG.computeIfAbsent(toId, k -> new CopyOnWriteArrayList<>()).add("Kamu: " + text);
        Thread.startVirtualThread(() -> { try { post("/chat", o); } catch (Exception ignored) {} });
    }

    public static void tick(MinecraftClient c) {
        if (BASE.isEmpty() || !premium() || c.player == null || c.getNetworkHandler() == null) return;
        if (++slow >= 600 && !busy) { // ~30 dtk: lapor "di game + server", ambil daftar pengguna badge & daftar teman
            slow = 0; busy = true;
            String me = key(c.player.getUuid()), name = c.player.getName().getString();
            var se = c.getCurrentServerEntry();
            String srv = se != null ? se.address : (c.isInSingleplayer() ? "Singleplayer" : "");
            List<String> ids = c.getNetworkHandler().getPlayerList().stream().map(e -> key(Compat.uuid(e))).limit(200).toList();
            Thread.startVirtualThread(() -> {
                try {
                    JsonObject h = new JsonObject();
                    h.addProperty("id", me); h.addProperty("name", name); h.addProperty("status", "game"); h.addProperty("server", srv);
                    post("/hello", h);
                    Set<String> now = new HashSet<>();
                    for (JsonElement e : JsonParser.parseString(get("/users?ids=" + String.join(",", ids))).getAsJsonObject().getAsJsonArray("users")) now.add(e.getAsString());
                    USERS.retainAll(now); USERS.addAll(now);
                    List<Friend> fl = new ArrayList<>();
                    for (JsonElement e : JsonParser.parseString(Files.readString(FRIENDS)).getAsJsonArray()) {
                        JsonObject f = e.getAsJsonObject();
                        fl.add(new Friend(f.get("name").getAsString(), f.get("id").getAsString(), f.get("status").getAsString(), f.get("server").getAsString(), f.get("tier").getAsString()));
                    }
                    friends = fl;
                } catch (Exception ignored) {} finally { busy = false; }
            });
        }
        if (++fast >= 100 && !busy2) { // ~5 dtk: chat masuk
            fast = 0; busy2 = true; String me = key(c.player.getUuid());
            Thread.startVirtualThread(() -> {
                try {
                    boolean first = since < 0;
                    for (JsonElement e : JsonParser.parseString(get("/inbox?id=" + me + "&since=" + Math.max(since, 0))).getAsJsonObject().getAsJsonArray("msgs")) {
                        JsonObject m = e.getAsJsonObject(); since = Math.max(since, m.get("n").getAsLong());
                        if (first) continue; // riwayat lama tidak ditampilkan saat baru join
                        String from = m.get("from").getAsString(), nm = m.get("name").getAsString(), tx = m.get("text").getAsString();
                        LOG.computeIfAbsent(from, k -> new CopyOnWriteArrayList<>()).add(nm + ": " + tx);
                        if (notify) c.execute(() -> c.inGameHud.getChatHud().addMessage(Text.literal("[VSZE] " + nm + ": " + tx)));
                    }
                    if (since < 0) since = 0;
                } catch (Exception ignored) {} finally { busy2 = false; }
            });
        }
    }
}
