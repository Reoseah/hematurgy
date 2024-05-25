package io.github.reoseah.hematurgy.item;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class RitualSickleItem extends SwordItem implements RitualHarvestCapableItem {
    public static final Item INSTANCE = new RitualSickleItem(HematurgyMaterials.RITUAL_SICKLE, 0, -1.6F, new Settings());

    public RitualSickleItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers(toolMaterial, attackDamage, attackSpeed)));
    }

    public static AttributeModifiersComponent createAttributeModifiers(ToolMaterial material, int attackDamage, float attackSpeed) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", attackDamage + material.getAttackDamage(), EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        RitualHarvestCapableItem.addTooltip(stack, tooltip);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            return RitualHarvestCapableItem.onUse(world, user, stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        RitualHarvestCapableItem.onPostHit(stack, target);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        RitualHarvestCapableItem.onInventoryTick(stack, world);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return RitualHarvestCapableItem.hasTarget(stack) ? "item.hematurgy.ritual_sickle.with_blood" : this.getTranslationKey();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isItemBarVisible(ItemStack stack) {
        return RitualHarvestCapableItem.hasTarget(stack) || super.isItemBarVisible(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarStep(ItemStack stack) {
        return RitualHarvestCapableItem.hasTarget(stack) ? RitualHarvestCapableItem.getItemBarStep(stack) : super.getItemBarStep(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarColor(ItemStack stack) {
        if (RitualHarvestCapableItem.hasTarget(stack)) {
            return RitualHarvestCapableItem.getItemBarColor(stack);
        }
        return super.getItemBarColor(stack);
    }

    @Override
    public boolean hasRitualHarvest(ItemStack stack) {
        return true;
    }
}

