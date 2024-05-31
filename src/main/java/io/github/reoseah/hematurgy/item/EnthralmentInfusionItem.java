package io.github.reoseah.hematurgy.item;

import io.github.reoseah.hematurgy.entity.Enthrallable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;

public class EnthralmentInfusionItem extends Item {
    public static final Item INSTANCE = new EnthralmentInfusionItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE));

    public EnthralmentInfusionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof MobEntity mob && mob.isAlive() && !mob.isBlocking() && mob.getMaxHealth() <= 100
                && mob instanceof Enthrallable enthrallable && enthrallable.getMasterUuid() == null) {
            if (!user.getWorld().isClient) {
                enthrallable.setMaster(user);
                stack.decrement(1);
            }
            return ActionResult.success(user.getWorld().isClient);
        }
        return ActionResult.PASS;
    }
}
