package io.github.reoseah.hematurgy.recipe;


import com.mojang.datafixers.util.Unit;
import io.github.reoseah.hematurgy.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        List<AbsorbableAbility> toAbsorb = AbsorbableAbility.getAbsorbableAbilities(target, material, world.getRegistryManager());
        return !toAbsorb.isEmpty();
    }

    @Override
    public void craft(Inventory inventory, World world, PlayerEntity player, BiConsumer<ItemStack, PlayerEntity> insertResult) {
        ItemStack target = inventory.getStack(0);
        if (!target.isOf(SentientBladeItem.INSTANCE)) {
            return;
        }

        ItemStack material = inventory.getStack(1);
        if (material.isOf(SentientBladeItem.INSTANCE)) {
            return;
        }
        List<AbsorbableAbility> toAbsorb = AbsorbableAbility.getAbsorbableAbilities(target, material, world.getRegistryManager());

        if (!toAbsorb.isEmpty()) {
            ItemStack result = target.copy();

            toAbsorb.forEach(ability -> ability.apply(result));

            inventory.removeStack(0, 1);
            inventory.removeStack(1, 1);

            insertResult.accept(result, player);
        }
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    /**
     * Enchantment that can be absorbed by Sentient Blade,
     * or an ability of another weapon that can be absorbed.
     */
    public interface AbsorbableAbility {
        boolean canCombine(AbsorbableAbility other);

        boolean canApply(ItemStack stack);

        void apply(ItemStack stack);

        static List<AbsorbableAbility> getAbsorbableAbilities(ItemStack target, ItemStack material, DynamicRegistryManager registryManager) {
            List<AbsorbableAbility> abilities = new ArrayList<>();

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

            List<AbsorbableAbility> applicable = new ArrayList<>();
            for (AbsorbableAbility ability : abilities) {
                if (ability.canApply(target)) {
                    applicable.add(ability);
                }
            }

            List<AbsorbableAbility> compatible = new ArrayList<>();
            for (AbsorbableAbility ability : applicable) {
                boolean isCompatible = true;
                for (AbsorbableAbility other : compatible) {
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

    public record EnchantmentAbility(RegistryEntry<Enchantment> enchantment, int level) implements AbsorbableAbility {
        @Override
        public boolean canCombine(AbsorbableAbility other) {
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

    public record Echoblade(int level) implements AbsorbableAbility {
        @Override
        public boolean canCombine(AbsorbableAbility other) {
            return other != LifeHarvest.INSTANCE;
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return !stack.contains(SentientBladeItem.HAS_ECHOBLADE);
        }

        @Override
        public void apply(ItemStack stack) {
            stack.set(SentientBladeItem.HAS_ECHOBLADE, Unit.INSTANCE);
            stack.set(EchobladeCapableItem.LEVEL, this.level);
            if (stack.contains(SentientBladeItem.HAS_RITUAL_HARVEST)) {
                stack.remove(SentientBladeItem.HAS_RITUAL_HARVEST);
                stack.remove(RitualHarvestCapableItem.TARGET);
            }
        }
    }

    public enum LifeHarvest implements AbsorbableAbility {
        INSTANCE;

        @Override
        public boolean canCombine(AbsorbableAbility other) {
            return !(other instanceof Echoblade);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return !stack.contains(SentientBladeItem.HAS_RITUAL_HARVEST);
        }

        @Override
        public void apply(ItemStack stack) {
            stack.set(SentientBladeItem.HAS_RITUAL_HARVEST, Unit.INSTANCE);
            stack.set(RitualHarvestCapableItem.TARGET, Optional.empty());
            if (stack.contains(SentientBladeItem.HAS_ECHOBLADE)) {
                stack.remove(SentientBladeItem.HAS_ECHOBLADE);
                stack.remove(EchobladeCapableItem.LEVEL);
            }
        }
    }
}