package io.github.reoseah.hematurgy.mixin;

import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean isIn(TagKey<Item> tag);

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void getTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (this.isIn(Hematurgy.HAS_RED_NAME)) {
            if (cir.getReturnValue().getFirst() instanceof MutableText name) {
                cir.getReturnValue().set(0, name.formatted(Formatting.RED));
            }
        }
    }
}
