package io.github.reoseah.hematurgy.mixin;

import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    private AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    public void updateResult(CallbackInfo ci) {
        ItemStack input = this.input.getStack(0);
        ItemStack material = this.input.getStack(1);
        if (input.isIn(Hematurgy.ONLY_REPAIRABLE_THROUGH_MEND_IRON) && !material.isEmpty() && !material.isOf(Items.ENCHANTED_BOOK)) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            ci.cancel();
        }
    }
}
