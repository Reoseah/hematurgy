package io.github.reoseah.hematurgy.item;

import com.mojang.serialization.Codec;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BloodCrystalSwordItem extends SwordItem {
    public static final Item INSTANCE = new BloodCrystalSwordItem(new Settings().maxCount(1));

    public static final DataComponentType<Long> LAST_TICK = DataComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build();

    public BloodCrystalSwordItem(Settings settings) {
        super(HematurgyMaterials.HEMOMANTICAL_SWORD, settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers(HematurgyMaterials.HEMOMANTICAL_SWORD, 4, -2.2f)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity) || world.isClient) {
            return;
        }
        var lastTick = stack.get(LAST_TICK);
        if (lastTick == null) {
            stack.set(LAST_TICK, world.getTime());
            return;
        }
        long timePassed = world.getTime() - lastTick;
        if (timePassed >= 20) {
            stack.set(LAST_TICK, world.getTime());

            EquipmentSlot equipmentSlot = slot == PlayerInventory.OFF_HAND_SLOT ? EquipmentSlot.OFFHAND : entity instanceof PlayerEntity player && slot == player.getInventory().selectedSlot ? EquipmentSlot.MAINHAND : EquipmentSlot.BODY;
            stack.damage(1, (PlayerEntity) entity, equipmentSlot);
        }
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }
}