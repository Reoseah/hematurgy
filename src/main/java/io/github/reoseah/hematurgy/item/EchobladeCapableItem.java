package io.github.reoseah.hematurgy.item;


import com.mojang.serialization.Codec;
import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;
import java.util.UUID;

public interface EchobladeCapableItem {
    DataComponentType<Integer> LEVEL = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build();
    int MAX_LEVEL = 50;
    UUID DAMAGE_MODIFIER_ID = UUID.fromString("1e2ff367-b0d3-4d6b-a7fc-280c56fba663");

    static ItemStack setEchobladeLevel(ItemStack stack, int level) {
        if (stack.isOf(RitualDaggerItem.INSTANCE)
                || stack.isOf(SentientBladeItem.INSTANCE) && stack.get(SpecialAbilityComponent.TYPE) == SpecialAbilityComponent.ECHOBLADE) {
            stack.set(LEVEL, level);

            var builder = AttributeModifiersComponent.builder();
            stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach(entry -> {
                var uuid = entry.modifier().uuid();
                if (uuid.equals(DAMAGE_MODIFIER_ID)) {
                    return;
                }
                builder.add(entry.attribute(),
                        new EntityAttributeModifier(uuid, entry.modifier().name(), entry.modifier().value(), entry.modifier().operation()),
                        entry.slot());
            });
            if (level > 0) {
                float damage = level / 2F;
                builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(DAMAGE_MODIFIER_ID,
                        "Echoblade modifier", damage, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND);
            }
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        }
        return stack;
    }

    static void onPostHit(ItemStack stack, LivingEntity target) {
        if (stack.isOf(RitualDaggerItem.INSTANCE)
                || stack.isOf(SentientBladeItem.INSTANCE) && stack.get(SpecialAbilityComponent.TYPE) == SpecialAbilityComponent.ECHOBLADE) {
            boolean targetDead = !target.isAlive();
            int level = Optional.ofNullable(stack.get(LEVEL)).orElse(0);
            if (targetDead && target.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
                if (level < MAX_LEVEL) {
                    setEchobladeLevel(stack, level + 1);
                }
            } else if (level > 0) {
                setEchobladeLevel(stack, level - 1);
            }
        }
    }
}