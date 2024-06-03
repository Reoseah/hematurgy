package io.github.reoseah.hematurgy.item;


import io.github.reoseah.hematurgy.Hematurgy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class SentientBladeItem extends SwordItem implements EchobladeCapableItem, RitualHarvestCapableItem {
    public static final Item INSTANCE = new SentientBladeItem(HematurgyMaterials.SENTIENT_BLADE, 3, -2.4F, new Settings());

    public SentientBladeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers(toolMaterial, attackDamage, attackSpeed)));
    }

    @Override
    public boolean hasRitualHarvest(ItemStack stack) {
        return stack.get(SpecialAbilityComponent.TYPE) == SpecialAbilityComponent.SICKLE;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        switch (stack.get(SpecialAbilityComponent.TYPE)) {
            case ECHOBLADE -> {
                var level = Text.translatable("enchantment.level." + stack.get(EchobladeCapableItem.LEVEL));
                tooltip.add(1, Text.translatable("hematurgy.echoblade.tooltip", level).formatted(Formatting.GRAY));
            }
            case SICKLE -> {
                tooltip.add(Text.translatable("hematurgy.ritual_harvest.tooltip").formatted(Formatting.GRAY));
                var target = stack.get(BloodSourceComponent.TYPE);
                if (target != null) {
                    target.appendTooltip(context, tooltip::add, type);
                }
            }
            case null -> {
            }
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        switch (stack.get(SpecialAbilityComponent.TYPE)) {
            case ECHOBLADE -> EchobladeCapableItem.onPostHit(stack, target);
            case SICKLE -> {
                boolean targetDead = !target.isAlive();
                if (targetDead && target.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
                    int max = Math.max(1, (int) (target.getMaxHealth() / 20F));
                    target.dropItem(BloodItem.INSTANCE, MathHelper.nextBetween(target.getRandom(), 1, max));
                }
            }
            case null -> {
            }
        }
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking() && stack.get(SpecialAbilityComponent.TYPE) == SpecialAbilityComponent.SICKLE) {
            if (stack.contains(BloodSourceComponent.TYPE)) {
                stack.remove(BloodSourceComponent.TYPE);
                return TypedActionResult.success(stack);
            } else {
                if (user.isCreative() || user.damage(world.getDamageSources().playerAttack(user), 2) && user.isAlive()) {
                    stack.set(BloodSourceComponent.TYPE, BloodSourceComponent.of(user));
                    return TypedActionResult.success(stack);
                }
                return TypedActionResult.fail(stack);
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        var bloodComponent = stack.get(BloodSourceComponent.TYPE);
        if (bloodComponent != null) {
            long age = world.getTime() - bloodComponent.timestamp();
            if (age > 20 * 30) {
                stack.remove(BloodSourceComponent.TYPE);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.contains(BloodSourceComponent.TYPE) || super.isItemBarVisible(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarStep(ItemStack stack) {
        if (stack.contains(BloodSourceComponent.TYPE)) {
            var bloodComponent = stack.get(BloodSourceComponent.TYPE);
            if (bloodComponent == null) {
                return 0;
            }

            return bloodComponent.getItemBarLength();
        } else {
            return super.getItemBarStep(stack);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarColor(ItemStack stack) {
        if (stack.contains(BloodSourceComponent.TYPE)) {
            var bloodComponent = stack.get(BloodSourceComponent.TYPE);
            if (bloodComponent == null) {
                return 0;
            }

            return bloodComponent.getItemBarColor();
        }
        return super.getItemBarColor(stack);
    }
}