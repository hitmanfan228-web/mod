package com.neverlose.client;

import com.neverlose.client.gui.GuiManager;
import com.neverlose.client.module.ModuleManager;
import com.neverlose.client.screen.NeverloseScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
        ModuleManager.init();

        // Register INSERT keybind to open the GUI
        // Adjusted the 4th parameter to pass a Category object instead of a String
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.neverlose.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_INSERT,
            // If Category is not an inner class of KeyBinding, you may need to import it.
            // Change MISC to your preferred category if this one doesn't exist in your mappings.
            KeyBinding.Category.MISC 
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen instanceof NeverloseScreen) {
                    client.currentScreen.close();
                } else if (client.currentScreen == null) {
                    client.setScreen(guiManager.createScreen(null));
                }
            }
            ModuleManager.tick(client);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) ->
            ModuleManager.hudRender(ctx, tickCounter));

        LOGGER.info("neverclick GUI ready. Press INSERT to open or close.");
    }

    public static GuiManager getGuiManager() {
        return guiManager;
    }
}