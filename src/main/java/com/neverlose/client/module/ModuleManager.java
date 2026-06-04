package com.neverlose.client.module;

import com.neverlose.client.gui.Module;
import com.neverlose.client.gui.ModuleRegistry;
import net.minecraft.client.MinecraftClient;

public final class ModuleManager {

    private static boolean initialized;

    private ModuleManager() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        ModuleRegistry.bootstrap();
    }

    public static void onModuleStateChanged(Module module, boolean enabled) {
        if (enabled) {
            ModuleEngine.onEnable(module);
        } else {
            ModuleEngine.onDisable(module);
        }
    }

    public static void tick(MinecraftClient client) {
        for (Module module : ModuleRegistry.getAll()) {
            if (module.isEnabled()) {
                ModuleEngine.onTick(client, module);
            }
        }
    }

    public static void hudRender(net.minecraft.client.gui.DrawContext ctx,
                                 net.minecraft.client.render.RenderTickCounter tickCounter) {
        for (Module module : ModuleRegistry.getAll()) {
            if (module.isEnabled()) {
                ModuleEngine.onHudRender(ctx, tickCounter, module);
            }
        }
    }
}
