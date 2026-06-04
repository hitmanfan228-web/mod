package com.neverlose.client;

import com.neverlose.client.gui.GuiManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeverloseClient implements ClientModInitializer {

    public static final String MOD_ID = "neverlose";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding openGuiKey;
    private static GuiManager guiManager;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Neverlose GUI initializing...");

        guiManager = new GuiManager();

        // Register INSERT keybind to open the GUI
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.neverlose.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_INSERT,
            "category.neverlose"
        ));

        // Tick event to check for keybind press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(guiManager.createScreen(client.currentScreen));
                }
            }
        });

        LOGGER.info("Neverlose GUI ready. Press INSERT in-game to open.");
    }

    public static GuiManager getGuiManager() {
        return guiManager;
    }
}
