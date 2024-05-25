package io.github.reoseah.hematurgy.network;

import io.github.reoseah.hematurgy.resource.book_element.SlotConfiguration;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ClientSlotLayoutPayload(SlotConfiguration[] layout) implements CustomPayload {
    public static final CustomPayload.Id<ClientSlotLayoutPayload> ID = CustomPayload.id("hematurgy:hemonomicon/client_slot_layout");
    public static final PacketCodec<RegistryByteBuf, ClientSlotLayoutPayload> CODEC = CustomPayload.codecOf(ClientSlotLayoutPayload::write, ClientSlotLayoutPayload::read);

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.layout.length);
        for (SlotConfiguration configuration : this.layout) {
            configuration.write(buf);
        }
    }

    public static ClientSlotLayoutPayload read(RegistryByteBuf buf) {
        var layout = new SlotConfiguration[buf.readVarInt()];
        for (int i = 0; i < layout.length; i++) {
            layout[i] = SlotConfiguration.read(buf);
        }
        return new ClientSlotLayoutPayload(layout);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
