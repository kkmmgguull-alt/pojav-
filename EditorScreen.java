package com.pojavhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class EditorScreen extends Screen {
    private int selectedIcon = 0;
    private boolean dragging = false;
    private double dragOffsetX, dragOffsetY;
    private boolean resizing = false;

    private static final int BASE_SIZE = 16;
    private static final int HANDLE_SIZE = 8;

    public EditorScreen() {
        super(Text.literal("PojavHUD Editor"));
        PojavHudClient.editorOpen = true;
    }

    @Override
    protected void init() {
        int btnY = this.height - 30;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            PojavHudClient.config.reset();
        }).dimensions(10, btnY, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> {
            PojavHudClient.config.save();
            this.close();
        }).dimensions(80, btnY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Prev"), b -> {
            selectedIcon = (selectedIcon + 8) % 9;
        }).dimensions(this.width - 130, btnY, 50, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Next"), b -> {
            selectedIcon = (selectedIcon + 1) % 9;
        }).dimensions(this.width - 70, btnY, 50, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x80000000);

        super.render(context, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory inv = client.player != null ? client.player.getInventory() : null;

        HudConfig.IconConfig[] icons = PojavHudClient.config.icons;

        context.drawText(this.textRenderer, "Selected: Icon " + (selectedIcon + 1) + " (Hotbar Slot " + (selectedIcon + 1) + ")", 10, 10, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Drag icon to move | Drag bottom-right corner to resize | Scroll to change opacity", 10, 22, 0xAAAAAA, false);
        context.drawText(this.textRenderer, String.format("Pos: (%.3f, %.3f)  Scale: %.2f  Opacity: %.2f",
                icons[selectedIcon].x, icons[selectedIcon].y, icons[selectedIcon].scale, icons[selectedIcon].opacity), 10, 34, 0xFFFFFF, false);

        for (int i = 0; i < 9; i++) {
            HudConfig.IconConfig icon = icons[i];
            int size = Math.max(8, (int) (BASE_SIZE * icon.scale));
            int x = (int) (icon.x * this.width) - size / 2;
            int y = (int) (icon.y * this.height) - size / 2;

            int bgColor = (i == selectedIcon) ? 0x80FFFF00 : 0x40000000;
            context.fill(x - 2, y - 2, x + size + 2, y + size + 2, bgColor);

            ItemStack stack = (inv != null) ? inv.getStack(i) : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                context.getMatrices().push();
                float scale = icon.scale;
                context.getMatrices().translate(x + size / 2.0, y + size / 2.0, 0);
                context.getMatrices().scale(scale, scale, 1.0f);
                context.getMatrices().translate(-8, -8, 0);
                context.setShaderColor(1.0f, 1.0f, 1.0f, icon.opacity);
                context.drawItem(stack, 0, 0);
                context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                context.getMatrices().pop();
            } else {
                context.drawCenteredTextWithShadow(this.textRenderer, String.valueOf(i + 1), x + size / 2, y + size / 2 - 4, 0x888888);
            }

            if (i == selectedIcon) {
                context.fill(x + size - HANDLE_SIZE, y + size - HANDLE_SIZE, x + size, y + size, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            HudConfig.IconConfig icon = PojavHudClient.config.icons[selectedIcon];
            int size = Math.max(8, (int) (BASE_SIZE * icon.scale));
            int x = (int) (icon.x * this.width) - size / 2;
            int y = (int) (icon.y * this.height) - size / 2;

            if (mouseX >= x + size - HANDLE_SIZE && mouseX <= x + size &&
                mouseY >= y + size - HANDLE_SIZE && mouseY <= y + size) {
                resizing = true;
                return true;
            }

            if (mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size) {
                dragging = true;
                dragOffsetX = mouseX - (icon.x * this.width);
                dragOffsetY = mouseY - (icon.y * this.height);
                return true;
            }

            for (int i = 0; i < 9; i++) {
                HudConfig.IconConfig ic = PojavHudClient.config.icons[i];
                int s = Math.max(8, (int) (BASE_SIZE * ic.scale));
                int ix = (int) (ic.x * this.width) - s / 2;
                int iy = (int) (ic.y * this.height) - s / 2;
                if (mouseX >= ix && mouseX <= ix + s && mouseY >= iy && mouseY <= iy + s) {
                    selectedIcon = i;
                    dragging = true;
                    dragOffsetX = mouseX - (ic.x * this.width);
                    dragOffsetY = mouseY - (ic.y * this.height);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
            resizing = false;
            PojavHudClient.config.save();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            HudConfig.IconConfig icon = PojavHudClient.config.icons[selectedIcon];
            if (dragging) {
                icon.x = (float) ((mouseX - dragOffsetX) / this.width);
                icon.y = (float) ((mouseY - dragOffsetY) / this.height);
                icon.x = Math.max(0.0f, Math.min(1.0f, icon.x));
                icon.y = Math.max(0.0f, Math.min(1.0f, icon.y));
                return true;
            }
            if (resizing) {
                int centerX = (int) (icon.x * this.width);
                int centerY = (int) (icon.y * this.height);
                double dist = Math.max(Math.abs(mouseX - centerX), Math.abs(mouseY - centerY));
                icon.scale = (float) Math.max(0.5, Math.min(4.0, dist / 8.0));
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        HudConfig.IconConfig icon = PojavHudClient.config.icons[selectedIcon];
        icon.opacity = (float) Math.max(0.1, Math.min(1.0, icon.opacity + verticalAmount * 0.05));
        return true;
    }

    @Override
    public void close() {
        PojavHudClient.editorOpen = false;
        PojavHudClient.config.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
