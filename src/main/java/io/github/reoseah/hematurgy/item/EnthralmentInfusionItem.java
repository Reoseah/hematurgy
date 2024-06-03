package io.github.reoseah.hematurgy.item;

import io.github.reoseah.hematurgy.Hematurgy;
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
        if (entity instanceof MobEntity mob
                && !mob.getType().isIn(Hematurgy.ENTHRALLMENT_IMMUNE)
                && mob.isAlive()
                && !mob.isBlocking()
                && mob.getMaxHealth() <= 100
                && mob instanceof Enthrallable enthrallable
                && enthrallable.hematurgy$getMasterUuid() == null) {
            if (!user.getWorld().isClient) {
                // TODO: track the creator of the item, not the user
                enthrallable.hematurgy$setMasterUuid(user.getUuid());
                stack.decrement(1);
                user.getInventory().insertStack(new ItemStack(SyringeItem.INSTANCE));
            }
            return ActionResult.success(user.getWorld().isClient);
        }
        return ActionResult.PASS;
    }
}
