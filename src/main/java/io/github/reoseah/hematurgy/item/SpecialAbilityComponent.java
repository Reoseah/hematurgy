package io.github.reoseah.hematurgy.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.DataComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

import java.util.Locale;
import java.util.function.IntFunction;

public enum SpecialAbilityComponent implements StringIdentifiable {
    SICKLE,
    ECHOBLADE;

    public static final Codec<SpecialAbilityComponent> CODEC = StringIdentifiable.createBasicCodec(SpecialAbilityComponent::values);
    public static final IntFunction<SpecialAbilityComponent> ID_TO_VALUE = ValueLists.<SpecialAbilityComponent>createIdToValueFunction(Enum::ordinal, SpecialAbilityComponent.values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, SpecialAbilityComponent> PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, Enum::ordinal);

    public static final DataComponentType<SpecialAbilityComponent> TYPE = DataComponentType.<SpecialAbilityComponent>builder().codec(CODEC).packetCodec(PACKET_CODEC).build();

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
