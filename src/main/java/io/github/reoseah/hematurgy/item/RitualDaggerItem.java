package io.github.reoseah.hematurgy.item;

import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.List;

public class RitualDaggerItem extends SwordItem implements EchobladeCapableItem {
    public static final Item INSTANCE = new RitualDaggerItem(HematurgyMaterials.RITUAL_DAGGER, 0, -2F, new Settings().rarity(Rarity.UNCOMMON));

    public RitualDaggerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers(toolMaterial, attackDamage, attackSpeed)));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        EchobladeCapableItem.insertTooltip(stack, tooltip);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        EchobladeCapableItem.onPostHit(stack, target);
        return true;
    }

    @Override
    public boolean hasEchoblade(ItemStack stack) {
        return true;
    }
}
