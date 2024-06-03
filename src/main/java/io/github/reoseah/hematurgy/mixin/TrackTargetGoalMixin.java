package io.github.reoseah.hematurgy.mixin;

import io.github.reoseah.hematurgy.entity.Enthrallable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalMixin {
    @Shadow
    @Final
    protected MobEntity mob;

    @Inject(method = "canTrack", at = @At("HEAD"), cancellable = true)
    public void canTrack(LivingEntity target, TargetPredicate targetPredicate, CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Enthrallable enthrallable && enthrallable.hematurgy$getMasterUuid() != null && enthrallable.getMaster(this.mob.getWorld()) == target) {
            cir.setReturnValue(false);
        }
    }
}