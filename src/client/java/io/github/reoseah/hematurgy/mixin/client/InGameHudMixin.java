package io.github.reoseah.hematurgy.mixin.client;

import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    private ItemStack currentStack;

    @ModifyArg(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;formatted(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;"), index = 0)
    private Formatting modifyTooltip(Formatting formatting) {
        if (this.currentStack.isIn(Hematurgy.HAS_RED_NAME)) {
            return Formatting.RED;
        }

        return formatting;
    }
}