# Neverlose GUI — Fabric Mod

A pink-themed in-game GUI shell for Minecraft 1.21.1 (Fabric).  
Press **INSERT** in-game to open the menu.

## Features

- Draggable, resizable frosted-glass window
- Sidebar navigation with tabs: Visuals, Scripts, Active, Settings
- Module card grid with single-click config, right-click toggle
- Live search bar (just start typing)
- Scroll support for long module lists
- Settings panel with toggles, sliders, and selects per module
- Accent color picker in Settings
- Save flash button
- No gameplay cheats — GUI shell only

## Modules (Visuals tab)

| Module       | Description                        |
|--------------|------------------------------------|
| Fullbright   | Gamma slider override              |
| Nametags     | Enhanced player nametags           |
| HUD Info     | FPS, coords, biome, direction      |
| Radar        | Mini-map radar overlay (UI only)   |
| Crosshair    | Custom crosshair style             |
| Ambience     | Time/weather override toggles      |

## Building

### Requirements
- JDK 21+
- Internet connection (Gradle downloads dependencies)

### Steps

```bash
# Clone / unzip the project, then:
cd neverlose-mod

# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

The built `.jar` will be at:
```
build/libs/neverlose-1.0.0.jar
```

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) into your mods folder
3. Drop `neverlose-1.0.0.jar` into your `.minecraft/mods/` folder
4. Launch Minecraft with the Fabric profile
5. Press **INSERT** in-game

## Project Structure

```
src/main/java/com/neverlose/client/
├── NeverloseClient.java        # Mod entrypoint, keybind registration
├── gui/
│   ├── GuiManager.java         # Tab state manager
│   ├── Module.java             # Module + Setting data classes
│   └── ModuleRegistry.java     # All registered modules
└── screen/
    └── NeverloseScreen.java    # Full GUI rendering & input
```

## Extending

To add a new module, open `ModuleRegistry.java` and add:

```java
Module myMod = register(new Module("MyMod", "Does something cool", "M", Module.Category.VISUALS));
myMod.settings.add(new Module.ToggleSetting("Some Toggle", "Description", false));
myMod.settings.add(new Module.SliderSetting("Some Slider", "Description", 50, 0, 100, 1));
```

It will appear in the Visuals grid automatically.
