package io.github.reoseah.hematurgy.recipe;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public abstract class HemonomiconRecipe implements Recipe<Inventory> {
    public static final RecipeType<HemonomiconRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "hematurgy:hemonomicon";
        }
    };

    public final Identifier utterance;
    public final int duration;

    protected HemonomiconRecipe(Identifier utterance, int duration) {
        this.utterance = utterance;
        this.duration = duration;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }


    @Override
    public ItemStack craft(Inventory inventory, RegistryWrapper.WrapperLookup lookup) {
        throw new UnsupportedOperationException();
    }

    public abstract ItemStack craft(Inventory inventory, World world, PlayerEntity player);

    public static class SpecialSerializer<T extends HemonomiconRecipe> implements RecipeSerializer<T> {
        private final BiFunction<Identifier, Integer, T> constructor;
        private final MapCodec<T> codec;
        private final PacketCodec<RegistryByteBuf, T> packetCodec;

        public SpecialSerializer(BiFunction<Identifier, Integer, T> constructor) {
            this.constructor = constructor;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance //
                    .group(Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                            Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration)) //
                    .apply(instance, this.constructor));
            this.packetCodec = new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, T value) {
                    buf.writeIdentifier(value.utterance);
                    buf.writeVarInt(value.duration);
                }

                @Override
                public T decode(RegistryByteBuf buf) {
                    Identifier utterance = buf.readIdentifier();
                    int duration = buf.readVarInt();
                    return constructor.apply(utterance, duration);
                }
            };
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> packetCodec() {
            return this.packetCodec;
        }
    }
}
