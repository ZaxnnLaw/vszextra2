package gg.vsze.extras.mixin;

import gg.vsze.extras.MenuMusic;
import gg.vsze.extras.ModUpdates;
import gg.vsze.extras.TitleBg;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Menu utama: pojok kanan atas ada "N mod update" + tombol Update now / Later (hanya di menu, bukan saat bermain). */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique private static boolean vsze$hidden;
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void vsze$init(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TitleBg.drawn = false;
        MenuMusic.ensure();

        int n = ModUpdates.count();
        boolean showUpdates = !vsze$hidden && n > 0;
        int musicY = 6;

        if (showUpdates) {
            Text label = Text.literal(n + " mod update");
            int lw = mc.textRenderer.getWidth(label);
            TextWidget tw = new TextWidget(label, mc.textRenderer);
            tw.setPosition(this.width - lw - 8, 8);
            this.addDrawableChild(tw);

            int bw = 62, gap = 4, y = 22;
            int x2 = this.width - bw - 6, x1 = x2 - bw - gap;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Update now"), b -> ModUpdates.requestAndQuit())
                    .dimensions(x1, y, bw, 18).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Later"), b -> {
                vsze$hidden = true;
                this.clearAndInit();
            }).dimensions(x2, y, bw, 18).build());
            musicY = 46; // di bawah notifikasi update
        }

        // Overlay musik: nama lagu + tombol Stop/Play (tanpa skip)
        int bw = 40, bh = 18;
        int bx = this.width - bw - 6;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(MenuMusic.isMuted() ? "Play" : "Stop"), b -> {
            boolean playing = MenuMusic.toggle();
            b.setMessage(Text.literal(playing ? "Stop" : "Play"));
        }).dimensions(bx, musicY, bw, bh).build());
        Text song = Text.literal("\u266A " + MenuMusic.TITLE);
        int sw = mc.textRenderer.getWidth(song);
        TextWidget st = new TextWidget(song, mc.textRenderer);
        st.setPosition(bx - sw - 6, musicY + 5);
        this.addDrawableChild(st);
    }
}
