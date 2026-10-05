package gg.vsze.extras;

import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Isi menu VSZE (logika sama di semua versi). Input mouse/keyboard ada di VszeScreen per versi. Menu VSZE di game: tab Friends (status, IP server, tier, chat) dan tab HUD (toggle). Dibuka Right Shift atau tombol di menu ESC. */
public abstract class VszeScreenBase extends Screen {
    private static final int W = 360, H = 236;
    private final Screen parent;
    private int tab, sel = -1;
    private TextFieldWidget input;

    protected VszeScreenBase(Screen parent) { super(Text.literal("VSZE")); this.parent = parent; }

    private int x0() { return (width - W) / 2; }
    private int y0() { return (height - H) / 2; }

    @Override
    protected void init() {
        int x = x0(), y = y0();
        addDrawableChild(ButtonWidget.builder(Text.literal("Friends"), b -> { tab = 0; sync(); }).dimensions(x + 8, y + 20, 64, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD"), b -> { tab = 1; sync(); }).dimensions(x + 76, y + 20, 44, 16).build());
        input = new TextFieldWidget(textRenderer, x + 146, y + H - 24, W - 154, 16, Text.empty());
        input.setMaxLength(200);
        addDrawableChild(input);
        sync();
    }

    private void sync() { input.visible = tab == 0 && sel >= 0 && sel < Presence.friends.size(); }

    private static final int CWD = (W - 24) / 2;
    private static int cx(int x, int i) { return x + 8 + (i % 2) * (CWD + 8); }
    private static int cy(int y, int i) { return y + 44 + (i / 2) * 22; }

    @Override
    public void renderBackground(DrawContext g, int mx, int my, float d) { // panel digambar di sini supaya tidak tertimpa background bawaan
        int x = x0(), y = y0();
        g.fill(0, 0, width, height, 0x99000000);
        g.fill(x, y, x + W, y + H, 0xF0101216);
        g.fill(x, y, x + W, y + 2, 0xFFFF7A3D);
    }

    @Override
    public void render(DrawContext g, int mx, int my, float d) {
        super.render(g, mx, my, d);
        int x = x0(), y = y0();
        g.drawText(textRenderer, "VSZE", x + 8, y + 7, 0xFFFF7A3D, true);
        if (tab == 1) {
            for (int i = 0; i < Modules.ALL.length; i++) {
                String id = Modules.ALL[i].id();
                boolean o = Modules.on(id);
                int rx = cx(x, i), ry = cy(y, i);
                g.fill(rx, ry, rx + CWD, ry + 19, o ? 0xFF2B2016 : 0xFF1F232B);
                g.fill(rx, ry, rx + 2, ry + 19, o ? 0xFFFF7A3D : 0xFF3A3F4A);
                g.drawText(textRenderer, textRenderer.trimToWidth(Modules.ALL[i].label(), CWD - 34), rx + 7, ry + 6, 0xFFFFFFFF, false);
                g.drawText(textRenderer, o ? "ON" : "OFF", rx + CWD - 22, ry + 6, o ? 0xFF55FF55 : 0xFF9AA0AA, false);
            }
            return;
        }
        if (!Presence.premium()) { g.drawText(textRenderer, "Friends khusus akun premium (Microsoft)", x + 8, y + 48, 0xFFFFAA55, false); return; }
        List<Presence.Friend> fl = Presence.friends;
        if (fl.isEmpty()) g.drawText(textRenderer, Presence.connected() ? "Belum ada teman (tambah lewat launcher)" : "Server presence belum diisi di launcher", x + 8, y + 48, 0xFF8A8F98, false);
        for (int i = 0; i < Math.min(fl.size(), 8); i++) {
            Presence.Friend f = fl.get(i);
            int ry = y + 44 + i * 20;
            if (i == sel) g.fill(x + 6, ry - 2, x + 138, ry + 18, 0xFF2B2016);
            g.drawText(textRenderer, (f.tier().isEmpty() ? "" : "[" + f.tier() + "] ") + f.name(), x + 10, ry, 0xFFFFFFFF, false);
            boolean game = f.status().equals("game"), lau = f.status().equals("launcher");
            String st = game ? "Di game" + (f.server().isEmpty() ? "" : " - " + f.server()) : lau ? "Di launcher" : "Offline";
            g.drawText(textRenderer, textRenderer.trimToWidth(st, 124), x + 10, ry + 9, game ? 0xFF55FF55 : lau ? 0xFF55AAFF : 0xFF8A8F98, false);
        }
        if (sel >= 0 && sel < fl.size()) {
            List<String> log = Presence.LOG.getOrDefault(fl.get(sel).id(), List.of());
            int from = Math.max(0, log.size() - 14);
            for (int i = from; i < log.size(); i++) g.drawText(textRenderer, textRenderer.trimToWidth(log.get(i), W - 158), x + 146, y + 44 + (i - from) * 10, 0xFFDDDDDD, false);
        }
    }

    /** Klik mouse; true = sudah ditangani. Dipanggil dari VszeScreen (beda signature per versi). */
    protected final boolean vszeClick(double mx, double my) {
        int x = x0(), y = y0();
        if (tab == 1) {
            for (int i = 0; i < Modules.ALL.length; i++) {
                int rx = cx(x, i), ry = cy(y, i);
                if (mx >= rx && mx < rx + CWD && my >= ry && my < ry + 19) { Modules.toggle(Modules.ALL[i].id()); return true; }
            }
        } else {
            for (int i = 0; i < Math.min(Presence.friends.size(), 8); i++) {
                int ry = y + 44 + i * 20;
                if (mx >= x + 6 && mx < x + 138 && my >= ry - 2 && my < ry + 18) { sel = i; sync(); setFocused(input); return true; }
            }
        }
        return false;
    }

    /** Enter ditekan: kirim chat. true = sudah ditangani. */
    protected final boolean vszeEnter() {
        if (input.visible && input.isFocused() && !input.getText().isBlank() && sel < Presence.friends.size()) {
            Presence.send(Presence.friends.get(sel).id(), input.getText().trim());
            input.setText("");
            return true;
        }
        return false;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
