package io.github.reoseah.hematurgy.mixin;

import io.github.reoseah.hematurgy.entity.ApotheosisEffect;
import io.github.reoseah.hematurgy.entity.ExtendedLivingEntity;
import io.github.reoseah.hematurgy.item.ApotheosisInfusionItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ExtendedLivingEntity {
    @Unique
    protected boolean hasApotheosis;
    @Unique
    protected long apotheosisTime;

    @Shadow
    public abstract void kill();

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Shadow
    public abstract boolean removeStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    private LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean hematurgy$hasApotheosis() {
        return this.hasApotheosis;
    }

    @Override
    public void hematurgy$applyApotheosis() {
        this.hasApotheosis = true;
        this.apotheosisTime = this.age;
        var effectEntry = Registries.STATUS_EFFECT.getEntry(ApotheosisEffect.INSTANCE);
        var duration = ApotheosisInfusionItem.DURATION - (this.age - this.apotheosisTime);
        this.addStatusEffect(new StatusEffectInstance(effectEntry, (int) duration, 0, false, false, true));
    }

    @Inject(at = @At("HEAD"), method = "tickStatusEffects")
    private void tickStatusEffects(CallbackInfo ci) {
        if (this.hasApotheosis) {
            if (this.age - this.apotheosisTime > ApotheosisInfusionItem.DURATION) {
                this.kill();
            } else if (this.age % 20 == 0) {
                var effectEntry = Registries.STATUS_EFFECT.getEntry(ApotheosisEffect.INSTANCE);
                if (!this.hasStatusEffect(effectEntry)) {
                    var duration = ApotheosisInfusionItem.DURATION - (this.age - this.apotheosisTime);
                    this.addStatusEffect(new StatusEffectInstance(effectEntry, (int) duration, 0, false, false, true));
                }
            }
        } else if (this.age % 20 == 0) {
            var effectEntry = Registries.STATUS_EFFECT.getEntry(ApotheosisEffect.INSTANCE);
            if (this.hasStatusEffect(effectEntry)) {
                this.removeStatusEffect(effectEntry);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    public void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.hasApotheosis) {
            nbt.putBoolean("Hematurgy$HasApotheosis", true);
            nbt.putLong("Hematurgy$ApotheosisTime", this.apotheosisTime);
        }
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    public void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.getBoolean("Hematurgy$HasApotheosis")) {
            this.hasApotheosis = true;
            this.apotheosisTime = nbt.getLong("Hematurgy$ApotheosisTime");
        } else {
            this.hasApotheosis = false;
        }
    }
}
