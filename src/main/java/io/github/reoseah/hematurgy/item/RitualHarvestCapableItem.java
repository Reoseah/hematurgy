package io.github.reoseah.hematurgy.item;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.UUID;

public interface RitualHarvestCapableItem {

    /*
     * Returns whether stack should have Ritual Harvest effect.
     *
     * Ritual Sickle always has it, Sentient Blade has if it had absorbed a ritual sickle.
     */
    boolean hasRitualHarvest(ItemStack stack);

    static boolean hasTarget(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack) && target != null;
    }

    static UUID getTargetUUID(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return target != null ? target.uuid() : null;
    }

    static Text getTargetName(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        if (target != null) {
            return Text.literal(target.name());
        }
        return null;
    }

    static boolean isTargetPlayer(ItemStack stack) {
        var target = stack.get(BloodSourceComponent.TYPE);
        return target != null && target.isPlayer();
    }

}
