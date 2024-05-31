package io.github.reoseah.hematurgy.item;

import io.github.reoseah.hematurgy.Hematurgy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class SyringeItem extends Item {
    public static final Item INSTANCE = new SyringeItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON));
    public static final int DAMAGE = 10;

    public SyringeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            if (stack.contains(BloodSourceComponent.TYPE)) {
                stack.remove(BloodSourceComponent.TYPE);
                stack.set(DataComponentTypes.RARITY, Rarity.UNCOMMON);
                return TypedActionResult.success(stack);
            } else {
                if (user.isCreative() || user.damage(world.getDamageSources().create(Hematurgy.HEMONOMICON_DAMAGE), DAMAGE) && user.isAlive()) {
                    stack.set(BloodSourceComponent.TYPE, BloodSourceComponent.of(user));
                    stack.set(DataComponentTypes.RARITY, Rarity.RARE);
                    return TypedActionResult.success(stack);
                }
                return TypedActionResult.fail(stack);
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null) {
            target.appendTooltip(context, tooltip::add, type);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null) {
            long age = world.getTime() - target.timestamp();
            if (age > 20 * 30) {
                stack.remove(BloodSourceComponent.TYPE);
                stack.set(DataComponentTypes.RARITY, Rarity.UNCOMMON);
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return stack.contains(BloodSourceComponent.TYPE) ? "item.hematurgy.syringe.with_blood" : this.getTranslationKey();
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
            var source = stack.get(BloodSourceComponent.TYPE);
            if (source == null) {
                return 0;
            }

            return source.getItemBarLength();
        } else {
            return super.getItemBarStep(stack);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarColor(ItemStack stack) {
        if (stack.contains(BloodSourceComponent.TYPE)) {
            var source = stack.get(BloodSourceComponent.TYPE);
            if (source == null) {
                return 0;
            }

            return source.getItemBarColor();
        }
        return super.getItemBarColor(stack);
    }

}
