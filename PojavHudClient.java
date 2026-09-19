package com.pojavhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PojavHudClient implements ClientModInitializer {
    public static final String MOD_ID = "pojavhud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding toggleKey;
    public static KeyBinding editorKey;

    public static boolean hudVisible = true;
    public static boolean editorOpen = false;

    public static HudConfig config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("PojavHUD initializing...");

        config = HudConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pojavhud.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.pojavhud"
        ));

        editorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pojavhud.editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.pojavhud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                hudVisible = !hudVisible;
            }
            while (editorKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new EditorScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (hudVisible && !editorOpen) {
                HudRenderer.render(drawContext, tickCounter.getTickDelta(false));
            }
        });

        LOGGER.info("PojavHUD initialized!");
    }
}
