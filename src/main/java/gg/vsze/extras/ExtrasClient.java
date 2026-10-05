package gg.vsze.extras;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExtrasClient implements ClientModInitializer {
    private static KeyBinding toggle, menu;
    /** Diset oleh /vszeextra menu; layar dibuka di tick berikutnya (setelah chat menutup dirinya). */
    public static volatile boolean pendingMenu;

    @Override
    public void onInitializeClient() {
        toggle = Compat.keybind("key.vsze.armorhud", GLFW.GLFW_KEY_J);
        Modules.zoomKey = Compat.keybind("key.vsze.zoom", GLFW.GLFW_KEY_C);
        Modules.lookKey = Compat.keybind("key.vsze.freelook", GLFW.GLFW_KEY_LEFT_ALT);
        menu = Compat.keybind("key.vsze.menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            while (menu.wasPressed()) if (c.currentScreen == null) c.setScreen(new VszeScreen(null));
            if (pendingMenu && c.currentScreen == null) { pendingMenu = false; c.setScreen(new VszeScreen(null)); }
            while (toggle.wasPressed()) Modules.toggle("armor");
            Modules.tick(c);
            HudModules.tick(c);
            Presence.tick(c);
        });
        // badge diri sendiri di pojok kanan atas (nametag sendiri tidak pernah digambar game)
        HudRenderCallback.EVENT.register((ctx, t) -> {
            MinecraftClient c = MinecraftClient.getInstance();
            if (!Presence.enabled || c.player == null || c.options.hudHidden || c.getDebugHud().shouldShowDebugHud()) return;
            Text tx = Presence.badge(c.player.getName());
            ctx.drawTextWithShadow(c.textRenderer, tx, c.getWindow().getScaledWidth() - c.textRenderer.getWidth(tx) - 6, 6, 0xFFFFFFFF);
        });
        // /vszeextra (setting HUD) dan alias /vszehud
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(VszeCommand.root("vszeextra"));
            dispatcher.register(VszeCommand.root("vszehud"));
        });
        HudRenderCallback.EVENT.register(ArmorHud::render);
        HudRenderCallback.EVENT.register(HudModules::render);
        // musik menu berhenti begitu masuk dunia / server
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { MenuMusic.stop(); Presence.onJoin(); });
    }
}
