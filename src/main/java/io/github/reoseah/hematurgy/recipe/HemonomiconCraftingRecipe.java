package io.github.reoseah.hematurgy.recipe;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiConsumer;

public class HemonomiconCraftingRecipe extends HemonomiconRecipe {
    public final DefaultedList<Ingredient> ingredients;
    public final ItemStack result;

    public HemonomiconCraftingRecipe(Identifier utterance, int duration, List<Ingredient> ingredients, ItemStack result) {
        super(utterance, duration);
        this.ingredients = DefaultedList.copyOf(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
        this.result = result;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        for (int i = 0; i < this.ingredients.size(); i++) {
            if (!this.ingredients.get(i).test(inventory.getStack(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void craft(Inventory inventory, World world, PlayerEntity player, BiConsumer<ItemStack, PlayerEntity> insertResult) {
        ItemStack result = this.getResult(world.getRegistryManager());

        int count = result.getMaxCount();
        for (int i = 0; i < this.ingredients.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                count = Math.min(count, inventory.getStack(i).getCount());
            }
        }
        for (int i = 0; i < this.ingredients.size(); i++) {
            inventory.removeStack(i, count);
        }
        result = result.copyWithCount(count);

        insertResult.accept(result, player);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registryManager) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<HemonomiconCraftingRecipe> {
        public static final MapCodec<HemonomiconCraftingRecipe> CODEC = RecordCodecBuilder //
                .mapCodec(instance -> instance.group( //
                                Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                                Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration), //
                                Codec.list(Ingredient.DISALLOW_EMPTY_CODEC).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients), //
                                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        ) //
                        .apply(instance, HemonomiconCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, HemonomiconCraftingRecipe> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public void encode(RegistryByteBuf buf, HemonomiconCraftingRecipe value) {
                buf.writeIdentifier(value.utterance);
                buf.writeVarInt(value.duration);
                buf.writeVarInt(value.ingredients.size());
                for (int i = 0; i < value.ingredients.size(); i++) {
                    Ingredient.PACKET_CODEC.encode(buf, value.ingredients.get(i));
                }
                ItemStack.PACKET_CODEC.encode(buf, value.result);
            }

            @Override
            public HemonomiconCraftingRecipe decode(RegistryByteBuf buf) {
                Identifier utterance = buf.readIdentifier();
                int duration = buf.readVarInt();
                int size = buf.readVarInt();
                DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
                for (int i = 0; i < size; i++) {
                    ingredients.set(i, Ingredient.PACKET_CODEC.decode(buf));
                }
                ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
                return new HemonomiconCraftingRecipe(utterance, duration, ingredients, result);
            }
        };

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<HemonomiconCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, HemonomiconCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
