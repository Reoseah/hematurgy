package io.github.reoseah.hematurgy.item;

import com.mojang.serialization.Codec;
import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Optional;

public class BloodItem extends Item {
    public static final DataComponentType<Optional<Long>> CREATION_TIME = DataComponentType.<Optional<Long>>builder().codec(Codecs.optional(Codec.LONG)).packetCodec(PacketCodecs.optional(PacketCodecs.VAR_LONG)).build();

    public static final Item INSTANCE = new BloodItem(20 * 30, Hematurgy.DECAYED_BLOOD, new Settings().maxCount(16).component(CREATION_TIME, null).food(new FoodComponent.Builder() //
            .nutrition(1) //
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 30, 1), 1) //
            .alwaysEdible().build()));

    public final int decayTicks;
    public final Item decayed;

    public BloodItem(int decayTicks, Item decayed, Settings settings) {
        super(settings);
        this.decayTicks = decayTicks;
        this.decayed = decayed;
    }
//
//    public static void tryDecayingItems(long worldTime, ScreenHandler screenHandler) {
//        if (screenHandler instanceof HemonomiconScreenHandler hemonomicon && hemonomicon.isUttering.get() == 1) {
//            boolean updated = false;
//            for (int i = 0; i < 16; i++) {
//                ItemStack stack = hemonomicon.inventory.getStack(i);
//                if (stack == null) {
//                    continue;
//                }
//                if (stack.getItem() instanceof DecayingItem) {
//                    NbtCompound nbt = stack.getNbt();
//                    if (nbt == null || !nbt.contains(CREATION_TIME_KEY)) {
//                        continue;
//                    }
//                    nbt.putLong(CREATION_TIME_KEY, nbt.getLong(CREATION_TIME_KEY) + 1);
//                    updated = true;
//                }
//            }
//            if (updated) {
//                screenHandler.sendContentUpdates();
//            }
//            return;
//        }
//        if (worldTime % 20 != 0) {
//            return;
//        }
//        try {
//            boolean updated = false;
//            for (Slot slot : screenHandler.slots) {
//                if (slot.inventory instanceof PlayerInventory) {
//                    continue;
//                }
//                ItemStack stack = slot.getStack();
//                if (stack == null) {
//                    continue;
//                }
//                if (stack.getItem() instanceof DecayingItem item) {
//                    NbtCompound nbt = stack.getOrCreateNbt();
//                    if (!nbt.contains(CREATION_TIME_KEY)) {
//                        nbt.putLong(CREATION_TIME_KEY, worldTime);
//                    }
//                    long age = worldTime - nbt.getLong(CREATION_TIME_KEY);
//                    if (age >= item.decayTicks) {
//                        slot.setStack(new ItemStack(item.decayed, stack.getCount()));
//                        updated = true;
//                    }
//                }
////                else if (stack.isOf(RitualSickleItem.INSTANCE)) {
////                    if (LifeHarvestCapableItem.hasTarget(stack)) {
////                        long targetSetTime = stack.getNbt().getLong(RitualSickleItem.TARGET_SET_TIME_KEY);
////
////                        long age = worldTime - targetSetTime;
////                        if (age > 20 * 30) {
////                            LifeHarvestCapableItem.removeTarget(stack);
////                            updated = true;
////                        }
////                    }
////                }
//            }
//            if (updated) {
//                screenHandler.sendContentUpdates();
//            }
//        } catch (Exception e) {
//            Hematurgy.LOGGER.error("Failed to decay items in screen handler", e);
//        }
//    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        long originTime = this.getOrSetCreationTime(stack, world.getTime());
        long age = world.getTime() - originTime;
        if (entity instanceof PlayerEntity player && age >= this.decayTicks) {
            player.getInventory().setStack(slot, new ItemStack(this.decayed, stack.getCount()));
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (stack.getItem() == otherStack.getItem()) {
            int count = clickType == ClickType.RIGHT ? 1 : Math.min(otherStack.getCount(), this.getMaxCount() - stack.getCount());
            long worldTime = player.getWorld().getTime();

            this.merge(stack, otherStack, count, worldTime);
            return true;
        }
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    protected void merge(ItemStack stack, ItemStack otherStack, int change, long worldTime) {
        int count = stack.getCount();
        int newCount = stack.getCount() + change;

        long time = this.getOrSetCreationTime(stack, worldTime);
        long otherTime = this.getOrSetCreationTime(otherStack, worldTime);
        long newTime = MathHelper.ceil(time * count / (double) newCount + otherTime * change / (double) newCount);

        otherStack.decrement(change);
        stack.setCount(newCount);
        stack.set(CREATION_TIME, Optional.of(newTime));
    }

    private long getOrSetCreationTime(ItemStack stack, long worldTime) {
        Optional<Long> time = stack.get(CREATION_TIME);
        if (time == null || time.isEmpty()) {
            stack.set(CREATION_TIME, Optional.of(worldTime));
            return worldTime;
        }
        return time.get();
    }

    public int getDecay(ItemStack stack) {
        Optional<Long> originTime = stack.get(CREATION_TIME);
        if (originTime == null || originTime.isEmpty()) {
            return 0;
        }
        World world = Hematurgy.safelyGetClientWorld();
        if (world == null) {
            return 0;
        }
        long age = world.getTime() - originTime.orElse(null);
        if (age >= this.decayTicks) {
            return 13;
        }
        return Math.round((float) age / (float) this.decayTicks * 13);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return this.getDecay(stack) != 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return 13 - this.getDecay(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int decay = this.getDecay(stack);
        float decayRatio = MathHelper.clamp(decay / 13F, 0F, 1F);

        float hue = MathHelper.lerp(decayRatio, 240, 360) / 360F;
        float saturation = MathHelper.lerp(decayRatio, 50, 100) / 100F;

        return MathHelper.hsvToRgb(hue, saturation, 1);
    }
}
