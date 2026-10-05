package gg.vsze.extras;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

/** Musik menu utama: loop, berhenti saat masuk game. Volume ikut slider "Music" di Options. */
public final class MenuMusic {
    public static final String TITLE = "migumilyrics";
    private static final Identifier ID = Identifier.of("vsze-extras", "menu_music");

    private static SoundInstance current;
    /** true kalau pemain menekan Stop; tetap senyap sampai ditekan Play lagi. */
    private static boolean muted;

    private MenuMusic() {}

    private static final class Loop extends AbstractSoundInstance {
        Loop() {
            super(ID, SoundCategory.MUSIC, Random.create());
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.relative = true;
            this.attenuationType = SoundInstance.AttenuationType.NONE;
        }
    }

    public static boolean isPlaying() {
        return current != null && MinecraftClient.getInstance().getSoundManager().isPlaying(current);
    }

    public static boolean isMuted() { return muted; }

    /** Dipanggil saat menu utama dibuka. */
    public static void ensure() {
        if (muted || isPlaying()) return;
        current = new Loop();
        MinecraftClient.getInstance().getSoundManager().play(current);
    }

    public static void stop() {
        if (current != null) MinecraftClient.getInstance().getSoundManager().stop(current);
        current = null;
    }

    /** Tombol overlay. Hasil: true = sekarang bermain. */
    public static boolean toggle() {
        if (muted) { muted = false; ensure(); return true; }
        muted = true; stop(); return false;
    }
}
