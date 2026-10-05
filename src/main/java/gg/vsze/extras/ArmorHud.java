package gg.vsze.extras;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/** ArmorHUD: helm, chestplate, leggings, boots + item di tangan, lengkap dengan sisa durability. Toggle: tombol J. */
public final class ArmorHud {
    public static boolean enabled = true;

    private ArmorHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (!Modules.on("armor") || p == null || mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud()) return;

        ItemStack[] rows = new ItemStack[] {
                p.getEquippedStack(EquipmentSlot.HEAD),  // helm
                p.getEquippedStack(EquipmentSlot.CHEST), // chestplate
                p.getEquippedStack(EquipmentSlot.LEGS),  // leggings
                p.getEquippedStack(EquipmentSlot.FEET),  // boots
                p.getMainHandStack()
        };

        int rowH = 18;
        int total = 0;
        for (ItemStack s : rows) if (!s.isEmpty()) total++;
        if (total == 0) return;

        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight(), h = total * rowH;
        int x = HudCfg.x("armor", sw - 78, sw, 60);
        int y = HudCfg.y("armor", sh / 2 - h / 2, sh, h);

        for (ItemStack s : rows) {
            if (s.isEmpty()) continue;
            ctx.drawItem(s, x, y);
            String label;
            int color = 0xFFFFFFFF;
            if (s.isDamageable()) {
                int max = s.getMaxDamage();
                int left = max - s.getDamage();
                float frac = max <= 0 ? 1f : (float) left / max;
                label = left + "";
                color = 0xFF000000 | colorFor(frac);
            } else {
                label = s.getCount() > 1 ? "x" + s.getCount() : "";
            }
            if (!label.isEmpty()) ctx.drawText(mc.textRenderer, label, x + 20, y + 5, color, true);
            y += rowH;
        }
    }

    /** hijau -> kuning -> merah sesuai sisa durability. */
    private static int colorFor(float f) {
        f = Math.max(0f, Math.min(1f, f));
        int r, g;
        if (f > 0.5f) { r = (int) (255 * (1f - f) * 2f); g = 255; }
        else { r = 255; g = (int) (255 * f * 2f); }
        return (r << 16) | (g << 8) | 0x30;
    }
}
