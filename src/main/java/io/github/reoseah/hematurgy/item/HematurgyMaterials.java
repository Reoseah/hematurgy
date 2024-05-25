package io.github.reoseah.hematurgy.item;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum HematurgyMaterials implements ToolMaterial {
    RITUAL_SICKLE(BlockTags.INCORRECT_FOR_IRON_TOOL, 65, 8F, 4F, 20, Ingredient.ofItems(Items.IRON_INGOT)),
    HEMOMANTICAL_SWORD(BlockTags.INCORRECT_FOR_STONE_TOOL,79, 8F, 8F,16, Ingredient.empty()),
    RITUAL_DAGGER(BlockTags.INCORRECT_FOR_IRON_TOOL,129, 8F, 4F,20, Ingredient.empty()),
    SENTIENT_BLADE(BlockTags.INCORRECT_FOR_IRON_TOOL, 1561, 8F, 3F, 0, Ingredient.empty()),
    ;
    final TagKey<Block> inverseTag;
    final int durability;
    final float miningSpeed;
    final float attackDamage;
    final int enchantability;
    final Ingredient repairIngredient;

    HematurgyMaterials(TagKey<Block> inverseTag, int durability, float miningSpeed, float attackDamage, int enchantability, Ingredient repairIngredient) {
        this.inverseTag = inverseTag;
        this.durability = durability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return this.inverseTag;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }
}
