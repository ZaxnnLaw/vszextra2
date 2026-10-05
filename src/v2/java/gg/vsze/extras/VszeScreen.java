package gg.vsze.extras;

import net.minecraft.client.gui.screen.Screen;

/** Input mouse/keyboard gaya lama (1.21 - 1.21.8). */
public class VszeScreen extends VszeScreenBase {
    public VszeScreen(Screen parent) { super(parent); }

    @Override
    public boolean mouseClicked(double mx, double my, int b) {
        if (vszeClick(mx, my)) return true;
        return super.mouseClicked(mx, my, b);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 257 && vszeEnter()) return true;
        return super.keyPressed(key, scan, mods);
    }
}
