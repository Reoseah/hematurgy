package io.github.reoseah.hematurgy.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.hematurgy.Hematurgy;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.TooltipAppender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.Consumer;

public record BloodSourceComponent(UUID uuid, String name, boolean isPlayer, long timestamp)
        implements TooltipAppender {
    public static final Codec<BloodSourceComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Uuids.CODEC.fieldOf("uuid").forGetter(BloodSourceComponent::uuid),
                    Codec.STRING.fieldOf("name").forGetter(BloodSourceComponent::name),
                    Codec.BOOL.fieldOf("is_player").forGetter(BloodSourceComponent::isPlayer),
                    Codec.LONG.fieldOf("timestamp").forGetter(BloodSourceComponent::timestamp))
            .apply(instance, BloodSourceComponent::new));

    public static final PacketCodec<PacketByteBuf, BloodSourceComponent> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public void encode(PacketByteBuf buf, BloodSourceComponent value) {
            buf.writeUuid(value.uuid());
            buf.writeString(value.name());
            buf.writeBoolean(value.isPlayer());
            buf.writeVarLong(value.timestamp());
        }

        @Override
        public BloodSourceComponent decode(PacketByteBuf buf) {
            return new BloodSourceComponent(buf.readUuid(), buf.readString(), buf.readBoolean(), buf.readVarLong());
        }
    };

    public static final DataComponentType<BloodSourceComponent> TYPE = DataComponentType.<BloodSourceComponent>builder()
            .codec(CODEC)
            .packetCodec(PACKET_CODEC)
            .build();

    public static BloodSourceComponent of(Entity entity) {
        return new BloodSourceComponent(
                entity.getUuid(),
                entity.getName().getString(),
                entity instanceof PlayerEntity,
                entity.getWorld().getTime()
        );
    }

    public int getItemBarLength() {
        return 13 - this.getAgeForDisplay();
    }

    public int getItemBarColor() {
        float ratio = MathHelper.clamp(this.getAgeForDisplay() / 13F, 0F, 1F);

        float hue = MathHelper.lerp(ratio, 240, 360) / 360F;
        float saturation = MathHelper.lerp(ratio, .5F, 1);

        return MathHelper.hsvToRgb(hue, saturation, 1);
    }

    private int getAgeForDisplay() {
        var world = Hematurgy.safelyGetClientWorld();
        if (world == null) {
            return 0;
        }
        long age = world.getTime() - this.timestamp();
        if (age >= 20 * 30) {
            return 13;
        }
        return Math.round((float) age / (float) (20 * 30) * 13);
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        var world = Hematurgy.safelyGetClientWorld();
        if (world != null) {
            if (this.isPlayer) {
                tooltip.accept(Text.translatable("hematurgy.blood_source.player", findPlayerName(this.uuid, world, this.name)).formatted(Formatting.DARK_RED));
            } else {
                tooltip.accept(Text.translatable("hematurgy.blood_source.entity", this.name).formatted(Formatting.DARK_RED));
            }
        }
    }

    private static Text findPlayerName(UUID uuid, World world, String fallback) {
        for (var player : world.getPlayers()) {
            if (player.getUuid().equals(uuid)) {
                return player.getName();
            }
        }
        return Text.of(fallback);
    }
}
