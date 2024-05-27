package io.github.reoseah.hematurgy.item;

import net.minecraft.item.Item;

public class SyringeItem extends Item {
    public static final Item INSTANCE = new SyringeItem(new Item.Settings().maxCount(1));

    public SyringeItem(Settings settings) {
        super(settings);
    }
}
