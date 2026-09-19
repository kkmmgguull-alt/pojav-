package com.pojavhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class HudRenderer {
    private static final int BASE_SIZE = 16;

    public static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        PlayerInventory inv = client.player.getInventory();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        HudConfig.IconConfig[] icons = PojavHudClient.config.icons;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            HudConfig.IconConfig icon = icons[i];
            int size = (int) (BASE_SIZE * icon.scale);
            int x = (int) (icon.x * screenWidth) - size / 2;
            int y = (int) (icon.y * screenHeight) - size / 2;

            if (icon.opacity <= 0.01f) continue;

            context.getMatrices().push();
            float scale = icon.scale;
            context.getMatrices().translate(x + size / 2.0, y + size / 2.0, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.getMatrices().translate(-8, -8, 0);

            // Draw with opacity via color
            context.setShaderColor(1.0f, 1.0f, 1.0f, icon.opacity);
            context.drawItem(stack, 0, 0);
            context.drawItemInSlot(client.textRenderer, stack, 0, 0);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            context.getMatrices().pop();
        }
    }
}
