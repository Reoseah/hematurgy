package io.github.reoseah.hematurgy.recipe;

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
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HemonomiconRepairRecipe extends HemonomiconRecipe {
    public final Ingredient item;
    public final Ingredient material;
    public final int repairAmount;

    protected HemonomiconRepairRecipe(Identifier utterance, int duration, Ingredient item, Ingredient material, int repairAmount) {
        super(utterance, duration);
        this.item = item;
        this.material = material;
        this.repairAmount = repairAmount;
    }

    @Override
    public ItemStack craft(Inventory inventory, World world, PlayerEntity player) {
        ItemStack item = inventory.getStack(0);
//        ItemStack material = inventory.getStack(1);
        ItemStack result = item.copy();
        result.setDamage(MathHelper.clamp(result.getDamage() - this.repairAmount, 0, result.getMaxDamage() - 1));

        inventory.removeStack(0, 1);
        inventory.removeStack(1, 1);

        return result;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        ItemStack item = inventory.getStack(0);
        ItemStack material = inventory.getStack(1);
        return this.item.test(item) && this.material.test(material);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.item.getMatchingStacks().length > 0 ? this.item.getMatchingStacks()[0].copy() : ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<HemonomiconRepairRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<HemonomiconRepairRecipe> CODEC = RecordCodecBuilder //
                .mapCodec(instance -> instance.group( //
                                Identifier.CODEC.fieldOf("utterance").forGetter(recipe -> recipe.utterance), //
                                Codecs.POSITIVE_INT.fieldOf("duration").forGetter(recipe -> recipe.duration), //
                                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("item").forGetter(recipe -> recipe.item), //
                                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("material").forGetter(recipe -> recipe.material), //
                                Codecs.POSITIVE_INT.fieldOf("repair_amount").forGetter(recipe -> recipe.repairAmount)
                        ) //
                        .apply(instance, HemonomiconRepairRecipe::new));

        public static final PacketCodec<RegistryByteBuf, HemonomiconRepairRecipe> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public void encode(RegistryByteBuf buf, HemonomiconRepairRecipe recipe) {
                Ingredient.PACKET_CODEC.encode(buf, recipe.item);
                buf.writeVarInt(recipe.duration);
                Ingredient.PACKET_CODEC.encode(buf, recipe.material);
                buf.writeIdentifier(recipe.utterance);
                buf.writeVarInt(recipe.repairAmount);

            }

            @Override
            public HemonomiconRepairRecipe decode(RegistryByteBuf buf) {
                Ingredient item = Ingredient.PACKET_CODEC.decode(buf);
                int duration = buf.readVarInt();
                Ingredient material = Ingredient.PACKET_CODEC.decode(buf);
                Identifier utterance = buf.readIdentifier();
                int repairAmount = buf.readVarInt();
                return new HemonomiconRepairRecipe(utterance, duration, item, material, repairAmount);
            }
        };

        @Override
        public MapCodec<HemonomiconRepairRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, HemonomiconRepairRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}