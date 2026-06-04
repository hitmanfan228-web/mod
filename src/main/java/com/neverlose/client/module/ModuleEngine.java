package com.neverlose.client.module;

import com.neverlose.client.gui.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * Client-side feature logic inspired by common utility mod patterns.
 * Original implementations — not copied from Meteor Client (GPL).
 */
public final class ModuleEngine {

    private static double savedGamma = 1.0;
    private static int savedFov = 70;
    private static int killAuraCooldown = 0;
    private static int antiAfkTicks = 0;

    private ModuleEngine() {}

    public static void onEnable(Module module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;

        switch (module.name) {
            case "Fullbright" -> {
                savedGamma = client.options.getGamma().getValue();
                applyFullbright(client, module);
            }
            case "Zoom" -> savedFov = client.options.getFov().getValue().intValue();
            default -> { }
        }
    }

    public static void onDisable(Module module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;

        switch (module.name) {
            case "Fullbright" -> client.options.getGamma().setValue(savedGamma);
            case "Zoom" -> {
                if (savedFov > 0) client.options.getFov().setValue(savedFov);
            }
            default -> { }
        }
    }

    public static void onTick(MinecraftClient client, Module module) {
        if (client.player == null || client.world == null) return;

        switch (module.name) {
            case "Fullbright" -> applyFullbright(client, module);
            case "Sprint" -> {
                if (client.player.forwardSpeed > 0 && !client.player.isSneaking()) {
                    client.player.setSprinting(true);
                }
            }
            case "Velocity" -> { /* applied on knockback via velocity scaling in tick when hurt */ }
            case "Kill Aura" -> tickKillAura(client, module);
            case "Auto Totem" -> tickAutoTotem(client);
            case "Anti AFK" -> tickAntiAfk(client);
            case "No Fall" -> {
                double minDistance = getSlider(module, "Min Distance", 2.0);
                
                if (client.player.fallDistance > minDistance && client.player.getVelocity().y < -0.1) {
                    if (getToggle(module, "Packet Only")) {
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendPacket(
                                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full(
                                    client.player.getX(), 
                                    client.player.getY(), 
                                    client.player.getZ(), 
                                    client.player.getYaw(), 
                                    client.player.getPitch(), 
                                    true,  // onGround (обманываем сервер, что мы на земле, чтобы сбросить урон)
                                    false  // horizontalCollision
                                )
                            );
                        }
                    } else {
                        // Легитный/Синглплеерный метод (сброс вектора скорости падения)
                        Vec3d v = client.player.getVelocity();
                        client.player.setVelocity(v.x, 0.0, v.z);
                        client.player.fallDistance = 0;
                    }
                }
            }
            case "Zoom" -> {
                int zoom = (int) getSlider(module, "Zoom", 30);
                client.options.getFov().setValue(zoom);
            }
            case "Ambience" -> {
                if (getToggle(module, "No Weather")) {
                    client.world.setRainGradient(0f);
                    client.world.setThunderGradient(0f);
                }
            }
            default -> { }
        }

        if (module.name.equals("Velocity") && client.player.hurtTime > 0) {
            double h = getSlider(module, "Horizontal", 0) / 100.0;
            Vec3d v = client.player.getVelocity();
            client.player.setVelocity(v.x * h, v.y, v.z * h);
        }
    }

    public static void onHudRender(DrawContext ctx, RenderTickCounter tickCounter, Module module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int x = 4;
        int y = 4;
        int line = 0;

        switch (module.name) {
            case "HUD Info" -> {
                if (getToggle(module, "FPS")) {
                    drawHudLine(ctx, x, y + line++ * 10, "FPS " + client.getCurrentFps());
                }
                if (getToggle(module, "Coordinates")) {
                    var p = client.player.getBlockPos();
                    drawHudLine(ctx, x, y + line++ * 10,
                        String.format("XYZ %d %d %d", p.getX(), p.getY(), p.getZ()));
                }
                if (getToggle(module, "Direction")) {
                    String dir = client.player.getHorizontalFacing().asString();
                    drawHudLine(ctx, x, y + line++ * 10, "Facing " + dir);
                }
                if (getToggle(module, "Biome") && client.world != null) {
                    var biome = client.world.getBiome(client.player.getBlockPos());
                    String biomeName = biome.getKey()
                        .map(k -> k.getValue().getPath())
                        .orElse("unknown");
                    drawHudLine(ctx, x, y + line++ * 10, "Biome " + biomeName);
                }
            }
            case "ESP" -> {
                double range = getSlider(module, "Range", 64);
                Box box = client.player.getBoundingBox().expand(range);
                int shown = 0;
                for (PlayerEntity pl : client.world.getEntitiesByClass(
                    PlayerEntity.class, box, p -> p != client.player && p.isAlive())) {
                    if (shown >= 8) break;
                    String line2 = pl.getName().getString()
                        + String.format(" %.1fm", Math.sqrt(client.player.squaredDistanceTo(pl)));
                    drawHudLine(ctx, x, y + shown++ * 10, line2);
                }
            }
            default -> { }
        }
    }

    private static void applyFullbright(MinecraftClient client, Module module) {
        double gamma = getSlider(module, "Gamma", 100) / 10.0;
        client.options.getGamma().setValue(Math.max(gamma, 1.0));
    }

private static void tickKillAura(MinecraftClient client, Module module) {
        if (client.player == null || client.world == null) return;

        if (killAuraCooldown > 0) {
            killAuraCooldown--;
            return;
        }

        // 1. Получаем базовый радиус Киллауры
        double auraRange = getSlider(module, "Range", 4.5);
        double range = auraRange;

        // 2. Синхронизация с Reach: если модуль Reach включен и его радиус больше, используем его
        com.neverlose.client.gui.Module reachModule = com.neverlose.client.gui.ModuleRegistry.getAll().stream()
                .filter(m -> m.name.equals("Reach") && m.isEnabled())
                .findFirst()
                .orElse(null);
                
        if (reachModule != null) {
            double reachRange = getSlider(reachModule, "Range", 4.0);
            if (reachRange > range) {
                range = reachRange;
            }
        }

        boolean players = getToggle(module, "Players");
        boolean mobs = getToggle(module, "Mobs");

        // 3. Создаем финальную переменную ДЛЯ использования внутри лямбда-выражений (Stream)
        final double fRange = range;

        // 4. Поиск потенциальных целей в радиусе вокруг позиции глаз игрока
        List<LivingEntity> targets = client.world.getEntitiesByClass(
            LivingEntity.class,
            net.minecraft.util.math.Box.of(client.player.getEyePos(), fRange * 2, fRange * 2, fRange * 2),
            e -> e != client.player && e.isAlive() && !e.isInvisible()
        ).stream()
            .filter(e -> {
                if (e instanceof net.minecraft.entity.player.PlayerEntity) return players;
                return mobs;
            })
            // Точная проверка дистанции по формуле квадрата расстояния (используем нашу fRange)
            .filter(e -> client.player.squaredDistanceTo(e) <= fRange * fRange)
            // Сортируем цели от ближайшей к самой дальней
            .sorted(java.util.Comparator.comparingDouble(client.player::squaredDistanceTo))
            .toList();

        if (targets.isEmpty()) return;

        // 5. Выбираем ближайшую цель (она первая в отсортированном списке)
        LivingEntity target = targets.get(0);

        // 6. Проверка скорости атаки (Attack Cooldown) игрока, чтобы бить с максимальным уроном
        if (client.player.getAttackCooldownProgress(0.0F) >= 1.0F) {
            // Отправляем пакет атаки через клиентского контроллера
            client.interactionManager.attackEntity(client.player, target);
            // Машем рукой (визуально для всех)
            client.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            
            // Сбрасываем внутренний КД модуля (например, на 2 тика, чтобы не спамить в один и тот же такт)
            killAuraCooldown = 2; 
        }
    }

    private static void tickAutoTotem(MinecraftClient client) {
        if (client.interactionManager == null || client.currentScreen != null) return; 
        if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        var handler = client.player.playerScreenHandler;
        for (int slot = 0; slot < handler.slots.size(); slot++) {
            if (handler.getSlot(slot).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                client.interactionManager.clickSlot(
                    handler.syncId,
                    slot,
                    40,
                    net.minecraft.screen.slot.SlotActionType.SWAP,
                    client.player
                );
                return;
            }
        }
    }

    private static void tickAntiAfk(MinecraftClient client) {
        if (++antiAfkTicks < 100) return;
        antiAfkTicks = 0;
        client.player.jump();
    }

    private static void drawHudLine(DrawContext ctx, int x, int y, String text) {
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x, y, 0xFFE8E8F0);
    }

    public static boolean getToggle(Module module, String label) {
        for (Module.Setting<?> s : module.settings) {
            if (s.label.equals(label) && s instanceof Module.ToggleSetting ts) {
                return ts.getValue();
            }
        }
        return false;
    }

    public static double getSlider(Module module, String label, double fallback) {
        for (Module.Setting<?> s : module.settings) {
            if (s.label.equals(label) && s instanceof Module.SliderSetting sl) {
                return sl.getValue();
            }
        }
        return fallback;
    }
}
