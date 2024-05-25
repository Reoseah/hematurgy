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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public interface EchobladeCapableItem {
    DataComponentType<Integer> LEVEL = DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build();
    int MAX_LEVEL = 50;
    UUID DAMAGE_MODIFIER_ID = UUID.fromString("1e2ff367-b0d3-4d6b-a7fc-280c56fba663");

    /**
     * Returns whether stack should have Echoblade effect.
     * <p>
     * Ritual Dagger always has it, Sentient Blade has if it had absorbed a ritual dagger.
     */
    boolean hasEchoblade(ItemStack stack);

    static int getEchobladeLevel(ItemStack stack) {
        if (stack.getItem() instanceof EchobladeCapableItem item && item.hasEchoblade(stack)) {
            var level = stack.get(LEVEL);
            return level != null ? level : 0;
        }
        return 0;
    }

    static ItemStack setEchobladeLevel(ItemStack stack, int level) {
        if (stack.getItem() instanceof EchobladeCapableItem item && item.hasEchoblade(stack)) {
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

    static void insertTooltip(ItemStack stack, List<Text> tooltip) {
        if (stack.getItem() instanceof EchobladeCapableItem item && item.hasEchoblade(stack)) {
            int level = EchobladeCapableItem.getEchobladeLevel(stack);
            tooltip.add(1, Text.translatable("hematurgy.echoblade.tooltip", Text.translatable("enchantment.level." + level)).formatted(Formatting.GRAY));
        }
    }

    static void onPostHit(ItemStack stack, LivingEntity target) {
        if (stack.getItem() instanceof EchobladeCapableItem item && item.hasEchoblade(stack)) {
            boolean targetDead = !target.isAlive();
            if (targetDead && target.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
                int level = EchobladeCapableItem.getEchobladeLevel(stack);
                if (level < EchobladeCapableItem.MAX_LEVEL) {
                    EchobladeCapableItem.setEchobladeLevel(stack, level + 1);
                }
            } else {
                int level = EchobladeCapableItem.getEchobladeLevel(stack);
                if (level > 0) {
                    EchobladeCapableItem.setEchobladeLevel(stack, level - 1);
                }
            }
        }
    }
}