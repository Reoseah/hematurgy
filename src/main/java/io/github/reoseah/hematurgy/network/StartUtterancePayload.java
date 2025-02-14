package io.github.reoseah.hematurgy.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StartUtterancePayload(Identifier id) implements CustomPayload {
    public static final CustomPayload.Id<StartUtterancePayload> ID = CustomPayload.id("hematurgy:hemonomicon/start_utterance");
    public static final PacketCodec<PacketByteBuf, StartUtterancePayload> CODEC = CustomPayload.codecOf(StartUtterancePayload::write, StartUtterancePayload::new);

    public StartUtterancePayload(PacketByteBuf buf) {
        this(Identifier.PACKET_CODEC.decode(buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        Identifier.PACKET_CODEC.encode(buf, this.id);
    }
}
