package io.github.reoseah.hematurgy.item;

import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RitualHarvestCapableItem {
    static void onPostHit(ItemStack stack, LivingEntity target) {
        if (stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack)) {
            boolean targetDead = !target.isAlive();
            if (targetDead && target.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
                int max = Math.max(1, (int) (target.getMaxHealth() / 20F));
                target.dropItem(BloodItem.INSTANCE, MathHelper.nextBetween(target.getRandom(), 1, max));
            }
        }
    }

    static boolean tryInteract(PlayerEntity player, World world, Hand hand, Entity entity) {
        if (!player.isSpectator() && entity.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack) && !hasTarget(stack)) {
                if (entity.damage(world.getDamageSources().playerAttack(player), 2) && entity.isAlive()) {
                    setRitualSickleTarget(entity, stack);
                }
                return true;
            }
        }
        return false;
    }

    /*
     * Returns whether stack should have Ritual Harvest effect.
     *
     * Ritual Sickle always has it, Sentient Blade has if it had absorbed a ritual sickle.
     */
    boolean hasRitualHarvest(ItemStack stack);

    static void addTooltip(ItemStack stack, List<Text> tooltip, Item.TooltipContext context, TooltipType type) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null && target.isPresent()) {
            target.get().appendTooltip(context, tooltip::add, type);
        }
    }

    static boolean hasTarget(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack) && target != null && target.isPresent();
    }

    static UUID getTargetUUID(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return target != null ? target.map(BloodSourceComponent::uuid).orElse(null) : null;
    }

    static Text getTargetName(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null && target.isPresent()) {
            return Text.literal(target.get().name());
        }
        return null;
    }

    static boolean isTargetPlayer(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return target != null && target.isPresent() && target.get().isPlayer();
    }

    static void removeTarget(ItemStack stack) {
        stack.remove(BloodSourceComponent.TYPE);
    }

    static void setRitualSickleTarget(Entity entity, ItemStack stack) {
        stack.set(BloodSourceComponent.TYPE, Optional.of(BloodSourceComponent.of(entity)));
    }

    static void onInventoryTick(ItemStack stack, World world) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null && target.isPresent()) {
            long age = world.getTime() - target.get().timestamp();
            if (age > 20 * 30) {
                removeTarget(stack);
            }
        }
    }

    static TypedActionResult<ItemStack> onUse(World world, PlayerEntity user, ItemStack stack) {
        if (hasTarget(stack)) {
            removeTarget(stack);
            return TypedActionResult.success(stack);
        } else {
            if (user.isCreative() || user.damage(world.getDamageSources().playerAttack(user), 2) && user.isAlive()) {
                setRitualSickleTarget(user, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.fail(stack);
        }
    }

    static int getItemBarStep(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target == null || target.isEmpty()) {
            return 0;
        }

        return target.get().getItemBarLength();
    }

    static int getItemBarColor(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target == null || target.isEmpty()) {
            return 0;
        }

        return target.get().getItemBarColor();
    }
}
