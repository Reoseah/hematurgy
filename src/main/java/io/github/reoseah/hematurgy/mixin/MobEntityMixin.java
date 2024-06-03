package io.github.reoseah.hematurgy.mixin;

import io.github.reoseah.hematurgy.entity.Enthrallable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Enthrallable {
    @Shadow
    protected @Final GoalSelector goalSelector;

    @Shadow
    protected @Final GoalSelector targetSelector;

    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "initDataTracker")
    protected void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(Enthrallable.MASTER_UUID, Optional.empty());
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    public void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.hematurgy$getMasterUuid() != null) {
            nbt.putUuid("ThrallMasterUUID", this.hematurgy$getMasterUuid());
        }
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    public void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        UUID uuid;
        if (nbt.containsUuid("ThrallMasterUUID")) {
            uuid = nbt.getUuid("ThrallMasterUUID");
        } else {
            String string = nbt.getString("ThrallMasterName");
            uuid = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }
        if (uuid != null) {
            try {
                this.hematurgy$setMasterUuid(uuid);
            } catch (Throwable ignored) {
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "dropEquipment")
    protected void onDropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (this.hematurgy$getMasterUuid() != null) {
            this.hematurgy$setMasterUuid(null);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;initGoals()V"), method = "<init>")
    private void addCustomAIGoals(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        this.goalSelector.add(4, new Enthrallable.FollowMasterGoal(mob, 2.0f, 10.0f, 1.0f));
        this.targetSelector.add(0, new Enthrallable.TrackMasterAttackerGoal(mob));
        this.targetSelector.add(1, new Enthrallable.AttackWithMasterGoal(mob));
    }

    @Inject(at = @At("HEAD"), method = "cannotDespawn", cancellable = true)
    public void onCannotDespawn(CallbackInfoReturnable<Boolean> cir) {
        if (this.hematurgy$getMasterUuid() != null) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public @Nullable UUID hematurgy$getMasterUuid() {
        return this.dataTracker.get(Enthrallable.MASTER_UUID).orElse(null);
    }

    @Override
    public void hematurgy$setMasterUuid(@Nullable UUID uuid) {
        this.dataTracker.set(Enthrallable.MASTER_UUID, Optional.ofNullable(uuid));
    }
}