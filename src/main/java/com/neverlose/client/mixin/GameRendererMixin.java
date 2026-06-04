package com.neverlose.client.mixin;

import com.neverlose.client.gui.Module;
import com.neverlose.client.gui.ModuleRegistry;
import com.neverlose.client.module.ModuleEngine;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyConstant(
        method = "updateTargetedEntity", // Ищем просто по имени метода
        constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 3.0),
        remap = true,
        require = 0 // Позволяет собрать проект, даже если в дев-среде имя временно отличается
    )
    private double modifyReachDistance(double original) {
        Module reachModule = ModuleRegistry.getAll().stream()
                .filter(m -> m.name.equals("Reach"))
                .findFirst()
                .orElse(null);

        if (reachModule != null && reachModule.isEnabled()) {
            return ModuleEngine.getSlider(reachModule, "Range", 4.5);
        }
        return original;
    }

    @ModifyConstant(
        method = "updateTargetedEntity",
        constant = @org.spongepowered.asm.mixin.injection.Constant(doubleValue = 9.0),
        remap = true,
        require = 0
    )
    private double modifySquaredReachDistance(double original) {
        Module reachModule = ModuleRegistry.getAll().stream()
                .filter(m -> m.name.equals("Reach"))
                .findFirst()
                .orElse(null);

        if (reachModule != null && reachModule.isEnabled()) {
            double customRange = ModuleEngine.getSlider(reachModule, "Range", 4.5);
            return customRange * customRange;
        }
        return original;
    }
}