package io.github.reoseah.hematurgy.item;


import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class SentientBladeItem extends SwordItem implements EchobladeCapableItem, RitualHarvestCapableItem {
    public static final Item INSTANCE = new SentientBladeItem(HematurgyMaterials.SENTIENT_BLADE, 3, -2.4F, new Settings());

    public static final DataComponentType<Unit> HAS_ECHOBLADE = DataComponentType.<Unit>builder().codec(Codec.unit(Unit.INSTANCE)).packetCodec(PacketCodec.unit(Unit.INSTANCE)).build();
    public static final DataComponentType<Unit> HAS_RITUAL_HARVEST = DataComponentType.<Unit>builder().codec(Codec.unit(Unit.INSTANCE)).packetCodec(PacketCodec.unit(Unit.INSTANCE)).build();

    public SentientBladeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers(toolMaterial, attackDamage, attackSpeed)));
    }

    @Override
    public boolean hasEchoblade(ItemStack stack) {
        return stack.getComponents().contains(HAS_ECHOBLADE);
    }

    @Override
    public boolean hasRitualHarvest(ItemStack stack) {
        return stack.getComponents().contains(HAS_RITUAL_HARVEST);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        EchobladeCapableItem.insertTooltip(stack, tooltip);
        if (this.hasRitualHarvest(stack)) {
            tooltip.add(Text.translatable("hematurgy.ritual_harvest.tooltip").formatted(Formatting.GRAY));
            RitualHarvestCapableItem.addTooltip(stack, tooltip);
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        if (this.hasEchoblade(stack)) {
            EchobladeCapableItem.onPostHit(stack, target);
        }
        if (this.hasRitualHarvest(stack)) {
            RitualHarvestCapableItem.onPostHit(stack, target);
        }
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (this.hasRitualHarvest(stack) && user.isSneaking()) {
            return RitualHarvestCapableItem.onUse(world, user, stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        RitualHarvestCapableItem.onInventoryTick(stack, world);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isItemBarVisible(ItemStack stack) {
        return RitualHarvestCapableItem.hasTarget(stack) || super.isItemBarVisible(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarStep(ItemStack stack) {
        return RitualHarvestCapableItem.hasTarget(stack) ? RitualHarvestCapableItem.getItemBarStep(stack) : super.getItemBarStep(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getItemBarColor(ItemStack stack) {
        if (RitualHarvestCapableItem.hasTarget(stack)) {
            return RitualHarvestCapableItem.getItemBarColor(stack);
        }
        return super.getItemBarColor(stack);
    }
}