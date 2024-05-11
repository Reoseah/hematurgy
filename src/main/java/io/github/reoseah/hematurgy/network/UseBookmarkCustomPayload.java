package io.github.reoseah.hematurgy.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record UseBookmarkCustomPayload(int page) implements CustomPayload {
    public static final CustomPayload.Id<UseBookmarkCustomPayload> ID = CustomPayload.id("hematurgy:hemonomicon/bookmark");
    public static final PacketCodec<PacketByteBuf, UseBookmarkCustomPayload> CODEC = CustomPayload.codecOf(UseBookmarkCustomPayload::write, UseBookmarkCustomPayload::new);

    public UseBookmarkCustomPayload(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.page);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
