package io.github.reoseah.hematurgy.mixin.client;

import io.github.reoseah.hematurgy.item.DecayingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(DecayingItem.class)
public class DecayingItemMixin {
    @Shadow
    private static @Final DataComponentType<Optional<Long>> CREATION_TIME;

    @Shadow
    private @Final int decayTicks;

    @SuppressWarnings("OverwriteAuthorRequired")
    @Overwrite
    public int getDecay(ItemStack stack) {
        Optional<Long> originTime = stack.get(CREATION_TIME);
        if (originTime == null || originTime.isEmpty()) {
            return 0;
        }
        World world = MinecraftClient.getInstance().world;
        if (world == null) {
            return 0;
        }
        long age = world.getTime() - originTime.orElse(null);
        if (age >= this.decayTicks) {
            return 13;
        }
        return Math.round((float) age / (float) this.decayTicks * 13);
    }

}
