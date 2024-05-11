package io.github.reoseah.hematurgy.resource.book;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class BookSlot {
    public final int x;
    public final int y;
    public final boolean output;
    protected final Ingredient ingredient;
    protected final Identifier background;

    public BookSlot(int x, int y, boolean output, Ingredient ingredient) {
        this(x, y, output, ingredient, null);
    }

    public BookSlot(int x, int y, boolean output, Ingredient ingredient, Identifier background) {
        this.x = x;
        this.y = y;
        this.background = background;
        this.ingredient = ingredient;
        this.output = output;
    }

    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return this.background != null ? Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, this.background) : null;
    }

    public BookSlot withOffset(int x, int y) {
        return new BookSlot(this.x + x, this.y + y, this.output, this.ingredient, this.background);
    }

    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.x);
        buf.writeVarInt(this.y);
        buf.writeBoolean(this.output);
        if (this.ingredient != null) {
            buf.writeBoolean(true);
            Ingredient.PACKET_CODEC.encode(buf, this.ingredient);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static BookSlot read(RegistryByteBuf buf) {
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        boolean output = buf.readBoolean();
        Ingredient ingredient = buf.readBoolean() ? Ingredient.PACKET_CODEC.decode(buf) : null;

        return new BookSlot(x, y, output, ingredient);
    }
}