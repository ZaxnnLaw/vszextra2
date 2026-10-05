package gg.vsze.extras;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;

/** Input mouse/keyboard gaya baru (1.21.9+: Click dan KeyInput). */
public class VszeScreen extends VszeScreenBase {
    public VszeScreen(Screen parent) { super(parent); }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (vszeClick(click.x(), click.y())) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == 257 && vszeEnter()) return true;
        return super.keyPressed(input);
    }
}
