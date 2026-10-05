package gg.vsze.extras;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.client.MinecraftClient;

/** Menghubungkan game dengan launcher: jumlah update mod (updates-ID.txt) dan permintaan "Update now". */
public final class ModUpdates {
    private static int cached = -2;

    private ModUpdates() {}

    private static Path dir() {
        return Paths.get(System.getProperty("user.home"), ".vsze");
    }

    private static String id() {
        return System.getProperty("vsze.instance", "");
    }

    /** Jumlah mod yang bisa diperbarui (0 kalau tidak ada / tidak dibuka lewat launcher). */
    public static int count() {
        if (cached != -2) return cached;
        cached = 0;
        try {
            if (id().isEmpty()) return 0;
            Path f = dir().resolve("updates-" + id() + ".txt");
            if (!Files.isRegularFile(f)) return 0;
            String first = new String(Files.readAllBytes(f), StandardCharsets.UTF_8).split("\\R", 2)[0].trim();
            cached = Integer.parseInt(first);
        } catch (Throwable ignored) {
        }
        return cached;
    }

    /** Minta launcher memperbarui mod lalu membuka game lagi, kemudian tutup game ini. */
    public static void requestAndQuit() {
        try {
            Files.createDirectories(dir());
            Files.write(dir().resolve("update-request.txt"), id().getBytes(StandardCharsets.UTF_8));
        } catch (Throwable ignored) {
        }
        MinecraftClient.getInstance().scheduleStop();
    }
}
