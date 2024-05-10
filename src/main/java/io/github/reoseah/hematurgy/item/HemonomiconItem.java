package io.github.reoseah.hematurgy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;

public class HemonomiconItem extends Item {
    public static final Item INSTANCE = new HemonomiconItem(new Item.Settings().rarity(Rarity.EPIC).maxCount(1).fireproof());

    protected HemonomiconItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }
}
