package io.github.reoseah.hematurgy.recipe;


import io.github.reoseah.hematurgy.Hematurgy;
import io.github.reoseah.hematurgy.item.BloodItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class DrawBloodRecipe extends HemonomiconRecipe {
    public static final RecipeSerializer<DrawBloodRecipe> SERIALIZER = new SpecialSerializer<>(DrawBloodRecipe::new);

    public DrawBloodRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return new ItemStack(BloodItem.INSTANCE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack craft(Inventory inventory, World world, PlayerEntity player) {
        if (player.isCreative()
                || player.damage(world.getDamageSources().create(Hematurgy.HEMONOMICON_DAMAGE), 10F)) {
            return new ItemStack(BloodItem.INSTANCE);
        }
        return ItemStack.EMPTY;
    }
}
