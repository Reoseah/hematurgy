package io.github.reoseah.hematurgy;

import io.github.reoseah.hematurgy.item.DecayingItem;
import io.github.reoseah.hematurgy.item.HemonomiconItem;
import io.github.reoseah.hematurgy.network.HemonomiconNetworking;
import io.github.reoseah.hematurgy.screen.HemonomiconScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hematurgy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hematurgy");


    public static final Item ASH = new Item(new Item.Settings());
    public static final Item BLOOD_SALT_MIXTURE = new Item(new Item.Settings());
    public static final Item BLOOD_SALT = new Item(new Item.Settings());
    public static final Item DECAYED_BLOOD = new Item(new Item.Settings());
    public static final Item BLOOD = new DecayingItem(20 * 30, DECAYED_BLOOD, new Item.Settings().maxCount(16).component(DecayingItem.CREATION_TIME, null).food(new FoodComponent.Builder() //
            .nutrition(1) //
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 30, 1), 1) //
            .alwaysEdible().build()));
    public static final Item HEMOALCHEMICAL_POISON = new Item(new Item.Settings()
            .food(new FoodComponent.Builder() //
                    .statusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 30, 3), 1) //
                    .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * 30, 3), 1) //
                    .statusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 30, 3), 1) //
                    .statusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 30, 3), 1) //
                    .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 30), 1) //
                    .alwaysEdible()
                    .build()));

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.hematurgy"))
            .icon(() -> new ItemStack(HemonomiconItem.INSTANCE))
            .entries((context, entries) -> {
                entries.add(HemonomiconItem.INSTANCE);
                entries.add(BLOOD);
                entries.add(ASH);
                entries.add(BLOOD_SALT_MIXTURE);
                entries.add(BLOOD_SALT);
                entries.add(DECAYED_BLOOD);
                entries.add(HEMOALCHEMICAL_POISON);
            })
            .build();

    public static final TagKey<Item> HAS_RED_NAME = TagKey.of(RegistryKeys.ITEM, new Identifier("hematurgy:has_red_name"));

    @Override
    public void onInitialize() {
        LOGGER.info("Start initialization...");

        Registry.register(Registries.ITEM, "hematurgy:hemonomicon", HemonomiconItem.INSTANCE);
        Registry.register(Registries.ITEM, "hematurgy:blood", BLOOD);
        Registry.register(Registries.ITEM, "hematurgy:ash", ASH);
        Registry.register(Registries.ITEM, "hematurgy:blood_salt_mixture", BLOOD_SALT_MIXTURE);
        Registry.register(Registries.ITEM, "hematurgy:blood_salt", BLOOD_SALT);
        Registry.register(Registries.ITEM, "hematurgy:decayed_blood", DECAYED_BLOOD);
        Registry.register(Registries.ITEM, "hematurgy:hemoalchemical_poison", HEMOALCHEMICAL_POISON);

        Registry.register(Registries.DATA_COMPONENT_TYPE, "hematurgy:current_page", HemonomiconItem.CURRENT_PAGE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, "hematurgy:creation_time", DecayingItem.CREATION_TIME);

        Registry.register(Registries.ITEM_GROUP, "hematurgy:main", ITEM_GROUP);

        Registry.register(Registries.SCREEN_HANDLER, "hematurgy:hemonomicon", HemonomiconScreenHandler.TYPE);

        LootTableEvents.MODIFY.register(Hematurgy::modifyLootTable);
        UseBlockCallback.EVENT.register(Hematurgy::interact);

        HemonomiconNetworking.register();

        LOGGER.info("Finished initialization");
    }

    private static void modifyLootTable(RegistryKey<LootTable> key, LootTable.Builder tableBuilder, LootTableSource source) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals("minecraft") || id.getNamespace().isEmpty()) {
            switch (id.getPath()) {
                case "chests/stronghold_library" -> {
                    LootPool.Builder pool = new LootPool.Builder()
                            .with(ItemEntry.builder(HemonomiconItem.INSTANCE));
                    tableBuilder.pool(pool);
                }
                case "chests/desert_pyramid", "chests/jungle_temple", "chests/simple_dungeon" -> {
                    LootPool.Builder pool = new LootPool.Builder()
                            .with(ItemEntry.builder(HemonomiconItem.INSTANCE))
                            .conditionally(RandomChanceLootCondition.builder(0.05F));
                    tableBuilder.pool(pool);
                }
                case "chests/nether_bridge" -> {
                    LootPool.Builder pool = new LootPool.Builder()
//                            .with(ItemEntry.builder(BROKEN_ANCIENT_SWORD).weight(9))
                            .with(ItemEntry.builder(HemonomiconItem.INSTANCE).weight(1))
                            .conditionally(RandomChanceLootCondition.builder(0.1F));
                    tableBuilder.pool(pool);
                }
            }
        }
    }

    private static ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (player.isSpectator()
                || !player.canModifyBlocks()
                || !player.canModifyAt(world, pos)) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);
        BlockState state = world.getBlockState(pos);
        BlockEntity be = world.getBlockEntity(pos);

        if (state.getBlock() instanceof LecternBlock
                && be instanceof LecternBlockEntity lectern) {
            boolean success = HemonomiconItem.handleLecternInteraction(player, world, hand, stack, pos, state, lectern);
            if (success) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}