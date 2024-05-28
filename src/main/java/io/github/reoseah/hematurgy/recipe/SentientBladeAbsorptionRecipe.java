package io.github.reoseah.hematurgy.recipe;


import io.github.reoseah.hematurgy.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SentientBladeAbsorptionRecipe extends HemonomiconRecipe {
    public static final RecipeSerializer<SentientBladeAbsorptionRecipe> SERIALIZER = new SpecialSerializer<>(SentientBladeAbsorptionRecipe::new);

    protected SentientBladeAbsorptionRecipe(Identifier utterance, int duration) {
        super(utterance, duration);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        ItemStack target = inventory.getStack(0);
        if (!target.isOf(SentientBladeItem.INSTANCE)) {
            return false;
        }

        ItemStack material = inventory.getStack(1);
        if (material.isOf(SentientBladeItem.INSTANCE)) {
            return false;
        }

        List<Ability> toAbsorb = Ability.getAbsorbableAbilities(target, material);
        return !toAbsorb.isEmpty();
    }

    @Override
    public ItemStack craft(Inventory inventory, World world, PlayerEntity player) {
        ItemStack target = inventory.getStack(0);
        if (!target.isOf(SentientBladeItem.INSTANCE)) {
            return ItemStack.EMPTY;
        }

        ItemStack material = inventory.getStack(1);
        if (material.isOf(SentientBladeItem.INSTANCE)) {
            return ItemStack.EMPTY;
        }
        List<Ability> abilities = Ability.getAbsorbableAbilities(target, material);

        if (!abilities.isEmpty()) {
            ItemStack result = target.copy();

            abilities.forEach(ability -> ability.apply(result));

            inventory.removeStack(0, 1);
            inventory.removeStack(1, 1);

            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public interface Ability {
        boolean canCombine(Ability other);

        boolean canApply(ItemStack stack);

        void apply(ItemStack stack);

        static List<Ability> getAbsorbableAbilities(ItemStack target, ItemStack material) {
            List<Ability> abilities = new ArrayList<>();

            EnchantmentHelper.getEnchantments(material).getEnchantmentsMap().forEach(entry -> {
                RegistryEntry<Enchantment> enchantment = entry.getKey();
                int level = entry.getIntValue();
                abilities.add(new EnchantmentAbility(enchantment, level));
            });

            if (material.isOf(RitualDaggerItem.INSTANCE)) {
                abilities.add(new Echoblade(EchobladeCapableItem.getEchobladeLevel(material)));
            } else if (material.isOf(RitualSickleItem.INSTANCE)) {
                abilities.add(LifeHarvest.INSTANCE);
            }

            List<Ability> applicable = new ArrayList<>();
            for (Ability ability : abilities) {
                if (ability.canApply(target)) {
                    applicable.add(ability);
                }
            }

            List<Ability> compatible = new ArrayList<>();
            for (Ability ability : applicable) {
                boolean isCompatible = true;
                for (Ability other : compatible) {
                    if (ability != other && !ability.canCombine(other)) {
                        isCompatible = false;
                        break;
                    }
                }
                if (isCompatible) {
                    compatible.add(ability);
                }
            }

            return compatible;
        }
    }

    public record EnchantmentAbility(RegistryEntry<Enchantment> enchantment, int level) implements Ability {
        @Override
        public boolean canCombine(Ability other) {
            return !(other instanceof EnchantmentAbility ability) || this.enchantment.value().canCombine(ability.enchantment.value());
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return this.enchantment.value().isAcceptableItem(stack) && EnchantmentHelper.getLevel(this.enchantment.value(), stack) < this.level;
        }

        @Override
        public void apply(ItemStack stack) {
            EnchantmentHelper.getEnchantments(stack).getEnchantments().forEach(entry -> {
                var enchantment = entry.value();
                if (!this.enchantment.value().canCombine(enchantment)) {
                    removeEnchantment(stack, enchantment);
                }
            });
            stack.addEnchantment(this.enchantment.value(), this.level);
        }
    }

    private static void removeEnchantment(ItemStack stack, Enchantment toRemove) {
        var enchantmentsComponent = stack.getEnchantments();
        var newEnchantments = new ItemEnchantmentsComponent.Builder(enchantmentsComponent);
        newEnchantments.remove(enchantment -> enchantment == toRemove);
        stack.set(DataComponentTypes.ENCHANTMENTS, newEnchantments.build());
    }

    public record Echoblade(int level) implements Ability {
        @Override
        public boolean canCombine(Ability other) {
            return other != LifeHarvest.INSTANCE;
        }

        @Override
        public boolean canApply(ItemStack stack) {
            var ability = stack.get(RitualWeaponAbilityComponent.TYPE);
            return ability != RitualWeaponAbilityComponent.ECHOBLADE;
        }

        @Override
        public void apply(ItemStack stack) {
            stack.set(RitualWeaponAbilityComponent.TYPE, RitualWeaponAbilityComponent.ECHOBLADE);
            stack.set(EchobladeCapableItem.LEVEL, this.level);
            if (stack.contains(BloodSourceComponent.TYPE)) {
                stack.remove(BloodSourceComponent.TYPE);
            }
        }
    }

    public enum LifeHarvest implements Ability {
        INSTANCE;

        @Override
        public boolean canCombine(Ability other) {
            return !(other instanceof Echoblade);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            var ability = stack.get(RitualWeaponAbilityComponent.TYPE);
            return ability != RitualWeaponAbilityComponent.HARVEST;
        }

        @Override
        public void apply(ItemStack stack) {
            stack.set(RitualWeaponAbilityComponent.TYPE, RitualWeaponAbilityComponent.HARVEST);
            if (stack.contains(EchobladeCapableItem.LEVEL)) {
                stack.remove(EchobladeCapableItem.LEVEL);
            }
        }
    }
}