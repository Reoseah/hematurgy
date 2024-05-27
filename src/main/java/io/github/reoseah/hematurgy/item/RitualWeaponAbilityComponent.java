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

public enum RitualWeaponAbilityComponent implements StringIdentifiable {
    HARVEST,
    ECHOBLADE;

    public static final Codec<RitualWeaponAbilityComponent> CODEC = StringIdentifiable.createBasicCodec(RitualWeaponAbilityComponent::values);
    public static final IntFunction<RitualWeaponAbilityComponent> ID_TO_VALUE = ValueLists.<RitualWeaponAbilityComponent>createIdToValueFunction(Enum::ordinal, RitualWeaponAbilityComponent.values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, RitualWeaponAbilityComponent> PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, Enum::ordinal);

    public static final DataComponentType<RitualWeaponAbilityComponent> TYPE = DataComponentType.<RitualWeaponAbilityComponent>builder().codec(CODEC).packetCodec(PACKET_CODEC).build();
    
    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
