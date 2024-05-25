package io.github.reoseah.hematurgy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RitualHarvestCapableItem {
    DataComponentType<Optional<Target>> TARGET = DataComponentType.<Optional<Target>>builder().codec(Codecs.optional(Target.CODEC)).packetCodec(PacketCodecs.optional(Target.PACKET_CODEC)).build();

    record Target(UUID uuid, String name, boolean isPlayer, long setTime) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("TargetUUID").forGetter(Target::uuid),
                Codec.STRING.fieldOf("TargetName").forGetter(Target::name),
                Codec.BOOL.fieldOf("TargetIsPlayer").forGetter(Target::isPlayer),
                Codec.LONG.fieldOf("TargetSetTime").forGetter(Target::setTime)
        ).apply(instance, Target::new));

        public static final PacketCodec<PacketByteBuf, Target> PACKET_CODEC = new PacketCodec<>() {
            @Override
            public void encode(PacketByteBuf buf, Target value) {
                buf.writeUuid(value.uuid());
                buf.writeString(value.name());
                buf.writeBoolean(value.isPlayer());
                buf.writeVarLong(value.setTime());
            }

            @Override
            public Target decode(PacketByteBuf buf) {
                return new Target(buf.readUuid(), buf.readString(), buf.readBoolean(), buf.readVarLong());
            }
        };
    }

    static void onPostHit(ItemStack stack, LivingEntity target) {
        if (stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack)) {
            boolean targetDead = !target.isAlive();
            if (targetDead && target.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
                int max = Math.max(1, (int) (target.getMaxHealth() / 20F));
                target.dropItem(BloodItem.INSTANCE, MathHelper.nextBetween(target.getRandom(), 1, max));
            }
        }
    }

    static boolean tryInteract(PlayerEntity player, World world, Hand hand, Entity entity) {
        if (!player.isSpectator() && entity.getType().isIn(Hematurgy.HAS_RITUAL_BLOOD)) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack) && !hasTarget(stack)) {
                if (entity.damage(world.getDamageSources().playerAttack(player), 2) && entity.isAlive()) {
                    setRitualSickleTarget(entity, stack);
                }
                return true;
            }
        }
        return false;
    }

    /*
     * Returns whether stack should have Ritual Harvest effect.
     *
     * Ritual Sickle always has it, Sentient Blade has if it had absorbed a ritual sickle.
     */
    boolean hasRitualHarvest(ItemStack stack);

    static void addTooltip(ItemStack stack, List<Text> tooltip) {
        var world = Hematurgy.safelyGetClientWorld();
        var target = stack.get(TARGET);
        if (world != null && target != null && target.isPresent()) {
            var uuid = target.map(Target::uuid).orElse(null);
            for (PlayerEntity player : world.getPlayers()) {
                if (player.getUuid().equals(uuid)) {
                    tooltip.add(Text.translatable("hematurgy.hemopathy_target.player", player.getName()).formatted(Formatting.DARK_RED));
                    return;
                }
            }
            var name = target.map(Target::name).orElse(null);
            if (name != null) {
                tooltip.add(Text.translatable("hematurgy.hemopathy_target.entity", name).formatted(Formatting.DARK_RED));
            } else {
                tooltip.add(Text.translatable("hematurgy.hemopathy_target.invalid").formatted(Formatting.DARK_RED));
            }
        }
    }

    static boolean hasTarget(ItemStack stack) {
        var target = stack.get(TARGET);
        return stack.getItem() instanceof RitualHarvestCapableItem item && item.hasRitualHarvest(stack) && target != null && target.isPresent();
    }

    static UUID getTargetUUID(ItemStack stack) {
        var target = stack.get(TARGET);
        return target != null ? target.map(Target::uuid).orElse(null) : null;
    }

    static Text getTargetName(ItemStack stack) {
        var target = stack.get(TARGET);
        if (target != null && target.isPresent()) {
            return Text.literal(target.get().name());
        }
        return null;
    }

    static boolean isTargetPlayer(ItemStack stack) {
        var target = stack.get(TARGET);
        return target != null && target.isPresent() && target.get().isPlayer();
    }

    static void removeTarget(ItemStack stack) {
        stack.remove(TARGET);
    }

    static void setRitualSickleTarget(Entity entity, ItemStack stack) {
        stack.set(TARGET, Optional.of(new Target(entity.getUuid(), entity.getName().getString(), entity instanceof PlayerEntity, entity.getWorld().getTime())));
    }

    static void onInventoryTick(ItemStack stack, World world) {
        var target = stack.get(TARGET);
        if (target != null && target.isPresent()) {
            long age = world.getTime() - target.get().setTime();
            if (age > 20 * 30) {
                removeTarget(stack);
            }
        }
    }

    static TypedActionResult<ItemStack> onUse(World world, PlayerEntity user, ItemStack stack) {
        if (RitualHarvestCapableItem.hasTarget(stack)) {
            RitualHarvestCapableItem.removeTarget(stack);
            return TypedActionResult.success(stack);
        } else {
            if (user.isCreative()
                    || user.damage(world.getDamageSources().playerAttack(user), 2) && user.isAlive()) {
                RitualHarvestCapableItem.setRitualSickleTarget(user, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.fail(stack);
        }
    }

    static int getItemBarStep(ItemStack stack) {
        return 13 - getDecay(stack);
    }

    static int getItemBarColor(ItemStack stack) {
        int decay = getDecay(stack);
        float decayRatio = MathHelper.clamp(decay / 13F, 0F, 1F);

        float hue = MathHelper.lerp(decayRatio, 240, 360) / 360F;
        float saturation = MathHelper.lerp(decayRatio, 50, 100) / 100F;

        return MathHelper.hsvToRgb(hue, saturation, 1);
    }

    private static int getDecay(ItemStack stack) {
        var target = stack.get(TARGET);
        if (target == null || target.isEmpty()) {
            return 0;
        }
        long originTime = target.get().setTime();
        World world = Hematurgy.safelyGetClientWorld();
        if (world == null) {
            return 0;
        }
        long age = world.getTime() - originTime;
        if (age >= 20 * 30) {
            return 13;
        }
        return Math.round((float) age / (float) (20 * 30) * 13);
    }
}
