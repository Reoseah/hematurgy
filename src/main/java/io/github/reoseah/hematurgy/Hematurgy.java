package io.github.reoseah.hematurgy;

import io.github.reoseah.hematurgy.item.HemonomiconItem;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hematurgy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hematurgy");

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.hematurgy"))
            .icon(() -> new ItemStack(HemonomiconItem.INSTANCE))
            .entries((context, entries) -> {
                entries.add(HemonomiconItem.INSTANCE);
            })
            .build();

    public static final TagKey<Item> HAS_RED_NAME = TagKey.of(RegistryKeys.ITEM, new Identifier("hematurgy:has_red_name"));


    @Override
    public void onInitialize() {
        LOGGER.info("Start initialization...");

        Registry.register(Registries.ITEM, "hematurgy:hemonomicon", HemonomiconItem.INSTANCE);

        Registry.register(Registries.ITEM_GROUP, "hematurgy:main", ITEM_GROUP);

        LOGGER.info("Finished initialization");
    }
}