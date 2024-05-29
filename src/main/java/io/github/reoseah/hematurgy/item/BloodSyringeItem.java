package io.github.reoseah.hematurgy.item;

import net.minecraft.item.Item;

public class BloodSyringeItem extends Item {
    public static final Item INSTANCE = new BloodSyringeItem(new Item.Settings().maxCount(1));
    
    public BloodSyringeItem(Settings settings) {
        super(settings);
    }
}
