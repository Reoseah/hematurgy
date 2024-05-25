package io.github.reoseah.hematurgy.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record StopUtterancePayload() implements CustomPayload {
    public static final CustomPayload.Id<StopUtterancePayload> ID = CustomPayload.id("hematurgy:hemonomicon/stop_utterance");
    public static final PacketCodec<PacketByteBuf, StopUtterancePayload> CODEC = PacketCodec.unit(new StopUtterancePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
