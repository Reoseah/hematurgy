package io.github.reoseah.hematurgy.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record UseBookmarkPayload(int page) implements CustomPayload {
    public static final CustomPayload.Id<UseBookmarkPayload> ID = CustomPayload.id("hematurgy:hemonomicon/use_bookmark");
    public static final PacketCodec<PacketByteBuf, UseBookmarkPayload> CODEC = CustomPayload.codecOf(UseBookmarkPayload::write, UseBookmarkPayload::new);

    public UseBookmarkPayload(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.page);
    }
}
