package io.github.reoseah.hematurgy.entity;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;

public class ApotheosisEffect extends StatusEffect {
    public static final String UUID = "56cb0320-ecb2-414f-8981-ad4df3360f21";
    public static final StatusEffect INSTANCE = new ApotheosisEffect()
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID, 2, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, UUID, 0.1, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, UUID, 1.0, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, UUID, 0.05, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_JUMP_STRENGTH, UUID, 0.1, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, UUID, 2, EntityAttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, UUID, 0.1, EntityAttributeModifier.Operation.ADD_VALUE);

    protected ApotheosisEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x800000, ParticleTypes.CRIMSON_SPORE);
    }
}
