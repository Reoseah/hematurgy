package io.github.reoseah.hematurgy.mixin.client;

import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Hematurgy.class)
public class HematurgyMixin {
    @Overwrite
    @SuppressWarnings("OverwriteAuthorRequired")
    public static World safelyGetClientWorld() {
        return MinecraftClient.getInstance().world;
    }

    @Overwrite
    @SuppressWarnings("OverwriteAuthorRequired")
    public static PlayerEntity safelyGetClientPlayer() {
        return MinecraftClient.getInstance().player;
    }
}
