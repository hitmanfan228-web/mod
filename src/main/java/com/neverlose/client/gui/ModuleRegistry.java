package com.neverlose.client.gui;

import java.util.ArrayList;
import java.util.List;

public class ModuleRegistry {

    private static final List<Module> modules = new ArrayList<>();
    private static boolean bootstrapped;

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        modules.clear();
        registerRage();
        registerLegit();
        registerVisuals();
        registerMisc();
    }

    private static void registerRage() {
        Module killAura = register(new Module("Kill Aura", "Attack nearby targets", "K", Module.Category.RAGE));
        killAura.settings.add(new Module.SliderSetting("Range", "Attack range", 4.5, 3, 6, 0.5));
        killAura.settings.add(new Module.ToggleSetting("Players", "Target players", true));
        killAura.settings.add(new Module.ToggleSetting("Mobs", "Target mobs", false));

        Module reach = register(new Module("Reach", "Extended reach display", "", Module.Category.RAGE));
            reach.settings.add(new Module.SliderSetting("Range", "Target reach distance", 4.0, 3.0, 6.0, 0.1));

        Module noFall = register(new Module("No Fall", "Disable fall damage", "N", Module.Category.LEGIT));
            noFall.settings.add(new Module.ToggleSetting("Packet Mode", "Spoof on-ground packets", true));
            noFall.settings.add(new Module.SliderSetting("Distance", "Min fall distance to activate", 2.0, 0.0, 10.0, 0.5));

        register(new Module("Auto Crystal", "Crystal PvP assist", "", Module.Category.RAGE));
        register(new Module("Auto Totem", "Hold totem in offhand", "T", Module.Category.RAGE));
        register(new Module("Surround", "Obsidian surround", "", Module.Category.RAGE));
        register(new Module("Bed Aura", "Auto bed explosions", "", Module.Category.RAGE));
        register(new Module("Anchor Aura", "Respawn anchor aura", "", Module.Category.RAGE));
    }

    private static void registerLegit() {
        register(new Module("Sprint", "Always sprint forward", "", Module.Category.LEGIT));
        register(new Module("Velocity", "Reduce knockback", "", Module.Category.LEGIT))
            .settings.add(new Module.SliderSetting("Horizontal", "Horizontal velocity %", 0, 0, 100, 5));
        register(new Module("No Slow", "Reduce slowdown effects", "", Module.Category.LEGIT));
        register(new Module("No Fall", "Disable fall damage", "", Module.Category.LEGIT));
        register(new Module("Auto Tool", "Switch best tool", "", Module.Category.LEGIT));
        register(new Module("Fast Place", "Place blocks faster", "", Module.Category.LEGIT));
        register(new Module("Safe Walk", "Prevent falling off edges", "", Module.Category.LEGIT));
    }

    private static void registerVisuals() {
        register(new Module("Fullbright", "Maximum brightness everywhere", "F", Module.Category.VISUALS))
            .settings.add(new Module.SliderSetting("Gamma", "Gamma override", 100, 1, 150, 5));

        Module nametags = register(new Module("Nametags", "Enhanced player nametags", "", Module.Category.VISUALS));
        nametags.settings.add(new Module.ToggleSetting("Health Bar", "Show HP below name", true));
        nametags.settings.add(new Module.ToggleSetting("Distance", "Show distance to player", true));
        nametags.settings.add(new Module.ToggleSetting("Ping", "Show player ping", false));

        Module hud = register(new Module("HUD Info", "Shows game info on screen", "H", Module.Category.VISUALS));
        hud.settings.add(new Module.ToggleSetting("FPS", "Show frames per second", true));
        hud.settings.add(new Module.ToggleSetting("Coordinates", "Show XYZ position", true));
        hud.settings.add(new Module.ToggleSetting("Biome", "Show current biome", false));
        hud.settings.add(new Module.ToggleSetting("Direction", "Show facing direction", true));

        Module esp = register(new Module("ESP", "See nearby players", "", Module.Category.VISUALS));
        esp.settings.add(new Module.SliderSetting("Range", "Detection range", 64, 16, 128, 8));

        register(new Module("Tracers", "Lines to entities", "", Module.Category.VISUALS));
        register(new Module("Zoom", "Zoom camera FOV", "Z", Module.Category.VISUALS))
            .settings.add(new Module.SliderSetting("Zoom", "FOV while active", 30, 10, 70, 5));

        Module crosshair = register(new Module("Crosshair", "Custom crosshair style", "C", Module.Category.VISUALS));
        crosshair.settings.add(new Module.SelectSetting("Style", "Crosshair type", "Cross",
            List.of("Cross", "Dot", "Circle", "Sniper")));

        Module ambience = register(new Module("Ambience", "Visual world effects", "", Module.Category.VISUALS));
        ambience.settings.add(new Module.ToggleSetting("Time Override", "Lock time of day", false));
        ambience.settings.add(new Module.ToggleSetting("No Weather", "Disable rain and snow", false));

        register(new Module("Freecam", "Camera detached from body", "", Module.Category.VISUALS));
        register(new Module("No Render", "Hide world elements", "", Module.Category.VISUALS));
    }

    private static void registerMisc() {
        register(new Module("Anti AFK", "Prevent idle kick", "", Module.Category.MISC));
        register(new Module("Auto Reconnect", "Reconnect when disconnected", "", Module.Category.MISC));
        register(new Module("Chest Stealer", "Loot chests quickly", "", Module.Category.MISC));
        register(new Module("Timer", "Modify game speed", "", Module.Category.MISC));
        register(new Module("Middle Click Friend", "Add friends with MMB", "", Module.Category.MISC));
        register(new Module("Discord RPC", "Show status on Discord", "", Module.Category.MISC));
    }

    private static Module register(Module m) {
        modules.add(m);
        return m;
    }

    public static List<Module> getAll() { return modules; }

    public static List<Module> getByCategory(Module.Category cat) {
        return modules.stream().filter(m -> m.category == cat).toList();
    }
}
