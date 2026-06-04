package com.neverlose.client.gui;

import java.util.ArrayList;
import java.util.List;

public class ModuleRegistry {

    private static final List<Module> modules = new ArrayList<>();

    static {
        // ── Visuals only ──────────────────────────────────────────
        register(new Module("Fullbright", "Maximum brightness everywhere", "F", Module.Category.VISUALS))
            .settings.add(new Module.SliderSetting("Gamma", "Gamma override", 100, 1, 100, 1));

        Module nametags = register(new Module("Nametags", "Enhanced player nametags", "", Module.Category.VISUALS));
        nametags.settings.add(new Module.ToggleSetting("Health Bar", "Show HP below name", true));
        nametags.settings.add(new Module.ToggleSetting("Distance", "Show distance to player", true));
        nametags.settings.add(new Module.ToggleSetting("Ping", "Show player ping", false));

        Module hud = register(new Module("HUD Info", "Shows game info on screen", "H", Module.Category.VISUALS));
        hud.settings.add(new Module.ToggleSetting("FPS", "Show frames per second", true));
        hud.settings.add(new Module.ToggleSetting("Coordinates", "Show XYZ position", true));
        hud.settings.add(new Module.ToggleSetting("Biome", "Show current biome", false));
        hud.settings.add(new Module.ToggleSetting("Direction", "Show facing direction", true));

        Module radar = register(new Module("Radar", "Mini-map radar overlay", "R", Module.Category.VISUALS));
        radar.settings.add(new Module.SliderSetting("Range", "Radar detection range", 64, 16, 256, 8));
        radar.settings.add(new Module.SelectSetting("Position", "Screen corner", "Top-Right",
            List.of("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right")));

        Module crosshair = register(new Module("Crosshair", "Custom crosshair style", "C", Module.Category.VISUALS));
        crosshair.settings.add(new Module.SelectSetting("Style", "Crosshair type", "Cross",
            List.of("Cross", "Dot", "Circle", "Sniper")));
        crosshair.settings.add(new Module.SliderSetting("Size", "Crosshair size", 5, 1, 20, 1));
        crosshair.settings.add(new Module.SliderSetting("Thickness", "Line thickness", 1, 1, 4, 1));

        Module ambience = register(new Module("Ambience", "Visual world effects", "", Module.Category.VISUALS));
        ambience.settings.add(new Module.ToggleSetting("Time Override", "Lock time of day", false));
        ambience.settings.add(new Module.SelectSetting("Time", "Time of day", "Noon",
            List.of("Dawn", "Noon", "Dusk", "Midnight")));
        ambience.settings.add(new Module.ToggleSetting("No Weather", "Disable rain & snow", false));
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
