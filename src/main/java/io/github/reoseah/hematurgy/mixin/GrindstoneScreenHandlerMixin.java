package io.github.reoseah.hematurgy.mixin;


import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    @Shadow
    private @Final Inventory result;
    @Shadow
    @Final
    Inventory input;

    private GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void updateResult(CallbackInfo ci) {
        ItemStack input = this.input.getStack(0);
        ItemStack material = this.input.getStack(1);
        if (input.isIn(Hematurgy.ONLY_REPAIRABLE_THROUGH_MEND_IRON) || material.isIn(Hematurgy.ONLY_REPAIRABLE_THROUGH_MEND_IRON)) {
            this.result.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            ci.cancel();
        }
    }
}
