package io.github.reoseah.hematurgy.item;

import com.google.common.collect.ImmutableList;
import io.github.reoseah.hematurgy.entity.ApotheosisEffect;
import io.github.reoseah.hematurgy.entity.ExtendedLivingEntity;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ApotheosisInfusionItem extends Item {
    public static final int DURATION = 60 * 60 * 20;

    public static final Item INSTANCE = new ApotheosisInfusionItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

    public ApotheosisInfusionItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking() && user instanceof ExtendedLivingEntity entity) {
            entity.hematurgy$applyApotheosis();
            return TypedActionResult.success(new ItemStack(SyringeItem.INSTANCE));
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        var effect = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(ApotheosisEffect.INSTANCE), DURATION);
        PotionContentsComponent.buildTooltip(ImmutableList.of(effect), tooltip::add, 1, context.getUpdateTickRate());
    }
}
